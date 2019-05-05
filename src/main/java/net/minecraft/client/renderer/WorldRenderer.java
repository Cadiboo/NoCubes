package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAbstractSkull;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockSign;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.IRenderChunkFactory;
import net.minecraft.client.renderer.chunk.ListedRenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.Particles;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBoneMeal;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class WorldRenderer implements IWorldEventListener, AutoCloseable, IResourceManagerReloadListener {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final ResourceLocation MOON_PHASES_TEXTURES = new ResourceLocation("textures/environment/moon_phases.png");
   private static final ResourceLocation SUN_TEXTURES = new ResourceLocation("textures/environment/sun.png");
   private static final ResourceLocation CLOUDS_TEXTURES = new ResourceLocation("textures/environment/clouds.png");
   private static final ResourceLocation END_SKY_TEXTURES = new ResourceLocation("textures/environment/end_sky.png");
   private static final ResourceLocation FORCEFIELD_TEXTURES = new ResourceLocation("textures/misc/forcefield.png");
   public static final EnumFacing[] FACINGS = EnumFacing.values();
   /** A reference to the Minecraft object. */
   private final Minecraft mc;
   /** The RenderEngine instance used by RenderGlobal */
   private final TextureManager textureManager;
   private final RenderManager renderManager;
   public WorldClient world;
   private Set<RenderChunk> chunksToUpdate = Sets.newLinkedHashSet();
   /** List of OpenGL lists for the current render pass */
   private List<WorldRenderer.ContainerLocalRenderInformation> renderInfos = Lists.newArrayListWithCapacity(69696);
   /** Global tile entities, always rendered (beacon, end teleporter, structures) */
   private final Set<TileEntity> setTileEntities = Sets.newHashSet();
   public ViewFrustum viewFrustum;
   /** The star GL Call list */
   private int starGLCallList = -1;
   /** OpenGL sky list */
   private int glSkyList = -1;
   /** OpenGL sky list 2 */
   private int glSkyList2 = -1;
   private final VertexFormat vertexBufferFormat;
   private VertexBuffer starVBO;
   private VertexBuffer skyVBO;
   private VertexBuffer sky2VBO;
   private final int field_204606_x = 28;
   private boolean cloudsNeedUpdate = true;
   private int glCloudsList = -1;
   private VertexBuffer cloudsVBO;
   /** counts the cloud render updates. Used with mod to stagger some updates */
   private int ticks;
   /**
    * Stores blocks currently being broken. Key is entity ID of the thing doing the breaking. Value is a
    * DestroyBlockProgress
    */
   private final Map<Integer, DestroyBlockProgress> damagedBlocks = Maps.newHashMap();
   /** Currently playing sounds.  Type:  HashMap<ChunkCoordinates, ISound> */
   private final Map<BlockPos, ISound> mapSoundPositions = Maps.newHashMap();
   private final TextureAtlasSprite[] destroyBlockIcons = new TextureAtlasSprite[10];
   private Framebuffer entityOutlineFramebuffer;
   /** Stores the shader group for the entity_outline shader */
   private ShaderGroup entityOutlineShader;
   private double frustumUpdatePosX = Double.MIN_VALUE;
   private double frustumUpdatePosY = Double.MIN_VALUE;
   private double frustumUpdatePosZ = Double.MIN_VALUE;
   private int frustumUpdatePosChunkX = Integer.MIN_VALUE;
   private int frustumUpdatePosChunkY = Integer.MIN_VALUE;
   private int frustumUpdatePosChunkZ = Integer.MIN_VALUE;
   private double lastViewEntityX = Double.MIN_VALUE;
   private double lastViewEntityY = Double.MIN_VALUE;
   private double lastViewEntityZ = Double.MIN_VALUE;
   private double lastViewEntityPitch = Double.MIN_VALUE;
   private double lastViewEntityYaw = Double.MIN_VALUE;
   private int cloudsCheckX = Integer.MIN_VALUE;
   private int cloudsCheckY = Integer.MIN_VALUE;
   private int cloudsCheckZ = Integer.MIN_VALUE;
   private Vec3d cloudsCheckColor = Vec3d.ZERO;
   private int cloudRenderMode = -1;
   private ChunkRenderDispatcher renderDispatcher;
   private ChunkRenderContainer renderContainer;
   private int renderDistanceChunks = -1;
   /** Render entities startup counter (init value=2) */
   private int renderEntitiesStartupCounter = 2;
   /** Count entities total */
   private int countEntitiesTotal;
   /** Count entities rendered */
   private int countEntitiesRendered;
   /** Count entities hidden */
   private int countEntitiesHidden;
   private boolean debugFixTerrainFrustum;
   private ClippingHelper debugFixedClippingHelper;
   private final Vector4f[] debugTerrainMatrix = new Vector4f[8];
   private final Vector3d debugTerrainFrustumPosition = new Vector3d();
   private boolean vboEnabled;
   private IRenderChunkFactory renderChunkFactory;
   private double prevRenderSortX;
   private double prevRenderSortY;
   private double prevRenderSortZ;
   private boolean displayListEntitiesDirty = true;
   private boolean entityOutlinesRendered;
   private final Set<BlockPos> setLightUpdates = Sets.newHashSet();

   public WorldRenderer(Minecraft mcIn) {
      this.mc = mcIn;
      this.renderManager = mcIn.getRenderManager();
      this.textureManager = mcIn.getTextureManager();
      this.textureManager.bindTexture(FORCEFIELD_TEXTURES);
      GlStateManager.texParameteri(3553, 10242, 10497);
      GlStateManager.texParameteri(3553, 10243, 10497);
      GlStateManager.bindTexture(0);
      this.updateDestroyBlockIcons();
      this.vboEnabled = OpenGlHelper.useVbo();
      if (this.vboEnabled) {
         this.renderContainer = new VboRenderList();
         this.renderChunkFactory = RenderChunk::new;
      } else {
         this.renderContainer = new RenderList();
         this.renderChunkFactory = ListedRenderChunk::new;
      }

      this.vertexBufferFormat = new VertexFormat();
      this.vertexBufferFormat.addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.POSITION, 3));
      this.generateStars();
      this.generateSky();
      this.generateSky2();
   }

   public void close() {
      if (this.entityOutlineShader != null) {
         this.entityOutlineShader.close();
      }

   }

   public void onResourceManagerReload(IResourceManager resourceManager) {
      this.updateDestroyBlockIcons();
   }

   private void updateDestroyBlockIcons() {
      TextureMap texturemap = this.mc.getTextureMap();
      this.destroyBlockIcons[0] = texturemap.getSprite(ModelBakery.LOCATION_DESTROY_STAGE_0);
      this.destroyBlockIcons[1] = texturemap.getSprite(ModelBakery.LOCATION_DESTROY_STAGE_1);
      this.destroyBlockIcons[2] = texturemap.getSprite(ModelBakery.LOCATION_DESTROY_STAGE_2);
      this.destroyBlockIcons[3] = texturemap.getSprite(ModelBakery.LOCATION_DESTROY_STAGE_3);
      this.destroyBlockIcons[4] = texturemap.getSprite(ModelBakery.LOCATION_DESTROY_STAGE_4);
      this.destroyBlockIcons[5] = texturemap.getSprite(ModelBakery.LOCATION_DESTROY_STAGE_5);
      this.destroyBlockIcons[6] = texturemap.getSprite(ModelBakery.LOCATION_DESTROY_STAGE_6);
      this.destroyBlockIcons[7] = texturemap.getSprite(ModelBakery.LOCATION_DESTROY_STAGE_7);
      this.destroyBlockIcons[8] = texturemap.getSprite(ModelBakery.LOCATION_DESTROY_STAGE_8);
      this.destroyBlockIcons[9] = texturemap.getSprite(ModelBakery.LOCATION_DESTROY_STAGE_9);
   }

   /**
    * Creates the entity outline shader to be stored in RenderGlobal.entityOutlineShader
    */
   public void makeEntityOutlineShader() {
      if (OpenGlHelper.shadersSupported) {
         if (ShaderLinkHelper.getStaticShaderLinkHelper() == null) {
            ShaderLinkHelper.setNewStaticShaderLinkHelper();
         }

         ResourceLocation resourcelocation = new ResourceLocation("shaders/post/entity_outline.json");

         try {
            this.entityOutlineShader = new ShaderGroup(this.mc.getTextureManager(), this.mc.getResourceManager(), this.mc.getFramebuffer(), resourcelocation);
            this.entityOutlineShader.createBindFramebuffers(this.mc.mainWindow.getFramebufferWidth(), this.mc.mainWindow.getFramebufferHeight());
            this.entityOutlineFramebuffer = this.entityOutlineShader.getFramebufferRaw("final");
         } catch (IOException ioexception) {
            LOGGER.warn("Failed to load shader: {}", resourcelocation, ioexception);
            this.entityOutlineShader = null;
            this.entityOutlineFramebuffer = null;
         } catch (JsonSyntaxException jsonsyntaxexception) {
            LOGGER.warn("Failed to load shader: {}", resourcelocation, jsonsyntaxexception);
            this.entityOutlineShader = null;
            this.entityOutlineFramebuffer = null;
         }
      } else {
         this.entityOutlineShader = null;
         this.entityOutlineFramebuffer = null;
      }

   }

   public void renderEntityOutlineFramebuffer() {
      if (this.isRenderEntityOutlines()) {
         GlStateManager.enableBlend();
         GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
         this.entityOutlineFramebuffer.framebufferRenderExt(this.mc.mainWindow.getFramebufferWidth(), this.mc.mainWindow.getFramebufferHeight(), false);
         GlStateManager.disableBlend();
      }

   }

   protected boolean isRenderEntityOutlines() {
      return this.entityOutlineFramebuffer != null && this.entityOutlineShader != null && this.mc.player != null;
   }

   private void generateSky2() {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      if (this.sky2VBO != null) {
         this.sky2VBO.deleteGlBuffers();
      }

      if (this.glSkyList2 >= 0) {
         GLAllocation.deleteDisplayLists(this.glSkyList2);
         this.glSkyList2 = -1;
      }

      if (this.vboEnabled) {
         this.sky2VBO = new VertexBuffer(this.vertexBufferFormat);
         this.renderSky(bufferbuilder, -16.0F, true);
         bufferbuilder.finishDrawing();
         bufferbuilder.reset();
         this.sky2VBO.bufferData(bufferbuilder.getByteBuffer());
      } else {
         this.glSkyList2 = GLAllocation.generateDisplayLists(1);
         GlStateManager.newList(this.glSkyList2, 4864);
         this.renderSky(bufferbuilder, -16.0F, true);
         tessellator.draw();
         GlStateManager.endList();
      }

   }

   private void generateSky() {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      if (this.skyVBO != null) {
         this.skyVBO.deleteGlBuffers();
      }

      if (this.glSkyList >= 0) {
         GLAllocation.deleteDisplayLists(this.glSkyList);
         this.glSkyList = -1;
      }

      if (this.vboEnabled) {
         this.skyVBO = new VertexBuffer(this.vertexBufferFormat);
         this.renderSky(bufferbuilder, 16.0F, false);
         bufferbuilder.finishDrawing();
         bufferbuilder.reset();
         this.skyVBO.bufferData(bufferbuilder.getByteBuffer());
      } else {
         this.glSkyList = GLAllocation.generateDisplayLists(1);
         GlStateManager.newList(this.glSkyList, 4864);
         this.renderSky(bufferbuilder, 16.0F, false);
         tessellator.draw();
         GlStateManager.endList();
      }

   }

   private void renderSky(BufferBuilder bufferBuilderIn, float posY, boolean reverseX) {
      int i = 64;
      int j = 6;
      bufferBuilderIn.begin(7, DefaultVertexFormats.POSITION);

      for(int k = -384; k <= 384; k += 64) {
         for(int l = -384; l <= 384; l += 64) {
            float f = (float)k;
            float f1 = (float)(k + 64);
            if (reverseX) {
               f1 = (float)k;
               f = (float)(k + 64);
            }

            bufferBuilderIn.pos((double)f, (double)posY, (double)l).endVertex();
            bufferBuilderIn.pos((double)f1, (double)posY, (double)l).endVertex();
            bufferBuilderIn.pos((double)f1, (double)posY, (double)(l + 64)).endVertex();
            bufferBuilderIn.pos((double)f, (double)posY, (double)(l + 64)).endVertex();
         }
      }

   }

   private void generateStars() {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      if (this.starVBO != null) {
         this.starVBO.deleteGlBuffers();
      }

      if (this.starGLCallList >= 0) {
         GLAllocation.deleteDisplayLists(this.starGLCallList);
         this.starGLCallList = -1;
      }

      if (this.vboEnabled) {
         this.starVBO = new VertexBuffer(this.vertexBufferFormat);
         this.renderStars(bufferbuilder);
         bufferbuilder.finishDrawing();
         bufferbuilder.reset();
         this.starVBO.bufferData(bufferbuilder.getByteBuffer());
      } else {
         this.starGLCallList = GLAllocation.generateDisplayLists(1);
         GlStateManager.pushMatrix();
         GlStateManager.newList(this.starGLCallList, 4864);
         this.renderStars(bufferbuilder);
         tessellator.draw();
         GlStateManager.endList();
         GlStateManager.popMatrix();
      }

   }

   private void renderStars(BufferBuilder bufferBuilderIn) {
      Random random = new Random(10842L);
      bufferBuilderIn.begin(7, DefaultVertexFormats.POSITION);

      for(int i = 0; i < 1500; ++i) {
         double d0 = (double)(random.nextFloat() * 2.0F - 1.0F);
         double d1 = (double)(random.nextFloat() * 2.0F - 1.0F);
         double d2 = (double)(random.nextFloat() * 2.0F - 1.0F);
         double d3 = (double)(0.15F + random.nextFloat() * 0.1F);
         double d4 = d0 * d0 + d1 * d1 + d2 * d2;
         if (d4 < 1.0D && d4 > 0.01D) {
            d4 = 1.0D / Math.sqrt(d4);
            d0 = d0 * d4;
            d1 = d1 * d4;
            d2 = d2 * d4;
            double d5 = d0 * 100.0D;
            double d6 = d1 * 100.0D;
            double d7 = d2 * 100.0D;
            double d8 = Math.atan2(d0, d2);
            double d9 = Math.sin(d8);
            double d10 = Math.cos(d8);
            double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
            double d12 = Math.sin(d11);
            double d13 = Math.cos(d11);
            double d14 = random.nextDouble() * Math.PI * 2.0D;
            double d15 = Math.sin(d14);
            double d16 = Math.cos(d14);

            for(int j = 0; j < 4; ++j) {
               double d17 = 0.0D;
               double d18 = (double)((j & 2) - 1) * d3;
               double d19 = (double)((j + 1 & 2) - 1) * d3;
               double d20 = 0.0D;
               double d21 = d18 * d16 - d19 * d15;
               double d22 = d19 * d16 + d18 * d15;
               double d23 = d21 * d12 + 0.0D * d13;
               double d24 = 0.0D * d12 - d21 * d13;
               double d25 = d24 * d9 - d22 * d10;
               double d26 = d22 * d9 + d24 * d10;
               bufferBuilderIn.pos(d5 + d25, d6 + d23, d7 + d26).endVertex();
            }
         }
      }

   }

   /**
    * set null to clear
    */
   public void setWorldAndLoadRenderers(@Nullable WorldClient worldClientIn) {
      if (this.world != null) {
         this.world.removeEventListener(this);
      }

      this.frustumUpdatePosX = Double.MIN_VALUE;
      this.frustumUpdatePosY = Double.MIN_VALUE;
      this.frustumUpdatePosZ = Double.MIN_VALUE;
      this.frustumUpdatePosChunkX = Integer.MIN_VALUE;
      this.frustumUpdatePosChunkY = Integer.MIN_VALUE;
      this.frustumUpdatePosChunkZ = Integer.MIN_VALUE;
      this.renderManager.setWorld(worldClientIn);
      this.world = worldClientIn;
      if (worldClientIn != null) {
         worldClientIn.addEventListener(this);
         this.loadRenderers();
      } else {
         this.chunksToUpdate.clear();
         this.renderInfos.clear();
         if (this.viewFrustum != null) {
            this.viewFrustum.deleteGlResources();
            this.viewFrustum = null;
         }

         if (this.renderDispatcher != null) {
            this.renderDispatcher.stopWorkerThreads();
         }

         this.renderDispatcher = null;
      }

   }

   /**
    * Loads all the renderers and sets up the basic settings usage
    */
   public void loadRenderers() {
      if (this.world != null) {
         if (this.renderDispatcher == null) {
            this.renderDispatcher = new ChunkRenderDispatcher();
         }

         this.displayListEntitiesDirty = true;
         this.cloudsNeedUpdate = true;
         BlockLeaves.setRenderTranslucent(this.mc.gameSettings.fancyGraphics);
         this.renderDistanceChunks = this.mc.gameSettings.renderDistanceChunks;
         boolean flag = this.vboEnabled;
         this.vboEnabled = OpenGlHelper.useVbo();
         if (flag && !this.vboEnabled) {
            this.renderContainer = new RenderList();
            this.renderChunkFactory = ListedRenderChunk::new;
         } else if (!flag && this.vboEnabled) {
            this.renderContainer = new VboRenderList();
            this.renderChunkFactory = RenderChunk::new;
         }

         if (flag != this.vboEnabled) {
            this.generateStars();
            this.generateSky();
            this.generateSky2();
         }

         if (this.viewFrustum != null) {
            this.viewFrustum.deleteGlResources();
         }

         this.stopChunkUpdates();
         synchronized(this.setTileEntities) {
            this.setTileEntities.clear();
         }

         this.viewFrustum = new ViewFrustum(this.world, this.mc.gameSettings.renderDistanceChunks, this, this.renderChunkFactory);
         if (this.world != null) {
            Entity entity = this.mc.getRenderViewEntity();
            if (entity != null) {
               this.viewFrustum.updateChunkPositions(entity.posX, entity.posZ);
            }
         }

         this.renderEntitiesStartupCounter = 2;
      }
   }

   protected void stopChunkUpdates() {
      this.chunksToUpdate.clear();
      this.renderDispatcher.stopChunkUpdates();
   }

   public void createBindEntityOutlineFbs(int width, int height) {
      this.setDisplayListEntitiesDirty();
      if (OpenGlHelper.shadersSupported) {
         if (this.entityOutlineShader != null) {
            this.entityOutlineShader.createBindFramebuffers(width, height);
         }

      }
   }

   public void renderEntities(Entity renderViewEntity, ICamera camera, float partialTicks) {
      int pass = net.minecraftforge.client.MinecraftForgeClient.getRenderPass();
      if (this.renderEntitiesStartupCounter > 0) {
         if (pass > 0) return;
         --this.renderEntitiesStartupCounter;
      } else {
         double d0 = renderViewEntity.prevPosX + (renderViewEntity.posX - renderViewEntity.prevPosX) * (double)partialTicks;
         double d1 = renderViewEntity.prevPosY + (renderViewEntity.posY - renderViewEntity.prevPosY) * (double)partialTicks;
         double d2 = renderViewEntity.prevPosZ + (renderViewEntity.posZ - renderViewEntity.prevPosZ) * (double)partialTicks;
         this.world.profiler.startSection("prepare");
         TileEntityRendererDispatcher.instance.prepare(this.world, this.mc.getTextureManager(), this.mc.fontRenderer, this.mc.getRenderViewEntity(), this.mc.objectMouseOver, partialTicks);
         this.renderManager.cacheActiveRenderInfo(this.world, this.mc.fontRenderer, this.mc.getRenderViewEntity(), this.mc.pointedEntity, this.mc.gameSettings, partialTicks);
         if (pass == 0) {
            this.countEntitiesTotal = 0;
            this.countEntitiesRendered = 0;
            this.countEntitiesHidden = 0;
         }
         Entity entity = this.mc.getRenderViewEntity();
         double d3 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
         double d4 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
         double d5 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
         TileEntityRendererDispatcher.staticPlayerX = d3;
         TileEntityRendererDispatcher.staticPlayerY = d4;
         TileEntityRendererDispatcher.staticPlayerZ = d5;
         this.renderManager.setRenderPosition(d3, d4, d5);
         this.mc.gameRenderer.enableLightmap();
         this.world.profiler.endStartSection("global");
         if (pass == 0) {
            this.countEntitiesTotal = this.world.getLoadedEntities();
         }
         for(int i = 0; i < this.world.weatherEffects.size(); ++i) {
            Entity entity1 = this.world.weatherEffects.get(i);
            if (!entity1.shouldRenderInPass(pass)) continue;
            ++this.countEntitiesRendered;
            if (entity1.isInRangeToRender3d(d0, d1, d2)) {
               this.renderManager.renderEntityStatic(entity1, partialTicks, false);
            }
         }

         this.world.profiler.endStartSection("entities");
         List<Entity> list = Lists.newArrayList();
         List<Entity> list1 = Lists.newArrayList();

         try (BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain()) {
            for(WorldRenderer.ContainerLocalRenderInformation worldrenderer$containerlocalrenderinformation : this.renderInfos) {
               Chunk chunk = this.world.getChunk(worldrenderer$containerlocalrenderinformation.renderChunk.getPosition());
               ClassInheritanceMultiMap<Entity> classinheritancemultimap = chunk.getEntityLists()[worldrenderer$containerlocalrenderinformation.renderChunk.getPosition().getY() / 16];
               if (!classinheritancemultimap.isEmpty()) {
                  for(Entity entity2 : classinheritancemultimap) {
                     if (!entity2.shouldRenderInPass(pass)) continue;
                     boolean flag = this.renderManager.shouldRender(entity2, camera, d0, d1, d2) || entity2.isRidingOrBeingRiddenBy(this.mc.player);
                     if (flag) {
                        boolean flag1 = this.mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase)this.mc.getRenderViewEntity()).isPlayerSleeping();
                        if ((entity2 != this.mc.getRenderViewEntity() || this.mc.gameSettings.thirdPersonView != 0 || flag1) && (!(entity2.posY >= 0.0D) || !(entity2.posY < 256.0D) || this.world.isBlockLoaded(blockpos$pooledmutableblockpos.setPos(entity2)))) {
                           ++this.countEntitiesRendered;
                           this.renderManager.renderEntityStatic(entity2, partialTicks, false);
                           if (this.isOutlineActive(entity2, entity, camera)) {
                              list.add(entity2);
                           }

                           if (this.renderManager.isRenderMultipass(entity2)) {
                              list1.add(entity2);
                           }
                        }
                     }
                  }
               }
            }
         }

         if (!list1.isEmpty()) {
            for(Entity entity3 : list1) {
               this.renderManager.renderMultipass(entity3, partialTicks);
            }
         }

         if (pass == 0 && this.isRenderEntityOutlines() && (!list.isEmpty() || this.entityOutlinesRendered)) {
            this.world.profiler.endStartSection("entityOutlines");
            this.entityOutlineFramebuffer.framebufferClear();
            this.entityOutlinesRendered = !list.isEmpty();
            if (!list.isEmpty()) {
               GlStateManager.depthFunc(519);
               GlStateManager.disableFog();
               this.entityOutlineFramebuffer.bindFramebuffer(false);
               RenderHelper.disableStandardItemLighting();
               this.renderManager.setRenderOutlines(true);

               for(int j = 0; j < list.size(); ++j) {
                  this.renderManager.renderEntityStatic(list.get(j), partialTicks, false);
               }

               this.renderManager.setRenderOutlines(false);
               RenderHelper.enableStandardItemLighting();
               GlStateManager.depthMask(false);
               this.entityOutlineShader.render(partialTicks);
               GlStateManager.enableLighting();
               GlStateManager.depthMask(true);
               GlStateManager.enableFog();
               GlStateManager.enableBlend();
               GlStateManager.enableColorMaterial();
               GlStateManager.depthFunc(515);
               GlStateManager.enableDepthTest();
               GlStateManager.enableAlphaTest();
            }

            this.mc.getFramebuffer().bindFramebuffer(false);
         }

         this.world.profiler.endStartSection("blockentities");
         RenderHelper.enableStandardItemLighting();

         TileEntityRendererDispatcher.instance.preDrawBatch();
         for(WorldRenderer.ContainerLocalRenderInformation worldrenderer$containerlocalrenderinformation1 : this.renderInfos) {
            List<TileEntity> list2 = worldrenderer$containerlocalrenderinformation1.renderChunk.getCompiledChunk().getTileEntities();
            if (!list2.isEmpty()) {
               for(TileEntity tileentity1 : list2) {
                  if (!tileentity1.shouldRenderInPass(pass) || !camera.isBoundingBoxInFrustum(tileentity1.getRenderBoundingBox())) continue;
                  TileEntityRendererDispatcher.instance.render(tileentity1, partialTicks, -1);
               }
            }
         }

         synchronized(this.setTileEntities) {
            for(TileEntity tileentity : this.setTileEntities) {
               if (!tileentity.shouldRenderInPass(pass) || !camera.isBoundingBoxInFrustum(tileentity.getRenderBoundingBox())) continue;
               TileEntityRendererDispatcher.instance.render(tileentity, partialTicks, -1);
            }
         }
         TileEntityRendererDispatcher.instance.drawBatch(pass);

         this.preRenderDamagedBlocks();

         for(DestroyBlockProgress destroyblockprogress : this.damagedBlocks.values()) {
            BlockPos blockpos = destroyblockprogress.getPosition();
            IBlockState iblockstate = this.world.getBlockState(blockpos);
            if (iblockstate.hasTileEntity()) {
               TileEntity tileentity2 = this.world.getTileEntity(blockpos);
               if (tileentity2 instanceof TileEntityChest && iblockstate.get(BlockChest.TYPE) == ChestType.LEFT) {
                  blockpos = blockpos.offset(iblockstate.get(BlockChest.FACING).rotateY());
                  tileentity2 = this.world.getTileEntity(blockpos);
               }

               if (tileentity2 != null && iblockstate.hasCustomBreakingProgress()) {
                  TileEntityRendererDispatcher.instance.render(tileentity2, partialTicks, destroyblockprogress.getPartialBlockDamage());
               }
            }
         }

         this.postRenderDamagedBlocks();
         this.mc.gameRenderer.disableLightmap();
         this.mc.profiler.endSection();
      }
   }

   /**
    * Checks if the given entity should have an outline rendered.
    */
   private boolean isOutlineActive(Entity entityIn, Entity viewer, ICamera camera) {
      boolean flag = viewer instanceof EntityLivingBase && ((EntityLivingBase)viewer).isPlayerSleeping();
      if (entityIn == viewer && this.mc.gameSettings.thirdPersonView == 0 && !flag) {
         return false;
      } else if (entityIn.isGlowing()) {
         return true;
      } else if (this.mc.player.isSpectator() && this.mc.gameSettings.keyBindSpectatorOutlines.isKeyDown() && entityIn instanceof EntityPlayer) {
         return entityIn.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(entityIn.getBoundingBox()) || entityIn.isRidingOrBeingRiddenBy(this.mc.player);
      } else {
         return false;
      }
   }

   /**
    * Gets the render info for use on the Debug screen
    */
   public String getDebugInfoRenders() {
      int i = this.viewFrustum.renderChunks.length;
      int j = this.getRenderedChunks();
      return String.format("C: %d/%d %sD: %d, L: %d, %s", j, i, this.mc.renderChunksMany ? "(s) " : "", this.renderDistanceChunks, this.setLightUpdates.size(), this.renderDispatcher == null ? "null" : this.renderDispatcher.getDebugInfo());
   }

   protected int getRenderedChunks() {
      int i = 0;

      for(WorldRenderer.ContainerLocalRenderInformation worldrenderer$containerlocalrenderinformation : this.renderInfos) {
         CompiledChunk compiledchunk = worldrenderer$containerlocalrenderinformation.renderChunk.compiledChunk;
         if (compiledchunk != CompiledChunk.DUMMY && !compiledchunk.isEmpty()) {
            ++i;
         }
      }

      return i;
   }

   /**
    * Gets the entities info for use on the Debug screen
    */
   public String getDebugInfoEntities() {
      return "E: " + this.countEntitiesRendered + "/" + this.countEntitiesTotal + ", B: " + this.countEntitiesHidden;
   }

   public void setupTerrain(Entity entityIn, float partialTicks, ICamera camera, int frameCount, boolean playerSpectator) {
      if (this.mc.gameSettings.renderDistanceChunks != this.renderDistanceChunks) {
         this.loadRenderers();
      }

      this.world.profiler.startSection("camera");
      double d0 = entityIn.posX - this.frustumUpdatePosX;
      double d1 = entityIn.posY - this.frustumUpdatePosY;
      double d2 = entityIn.posZ - this.frustumUpdatePosZ;
      if (this.frustumUpdatePosChunkX != entityIn.chunkCoordX || this.frustumUpdatePosChunkY != entityIn.chunkCoordY || this.frustumUpdatePosChunkZ != entityIn.chunkCoordZ || d0 * d0 + d1 * d1 + d2 * d2 > 16.0D) {
         this.frustumUpdatePosX = entityIn.posX;
         this.frustumUpdatePosY = entityIn.posY;
         this.frustumUpdatePosZ = entityIn.posZ;
         this.frustumUpdatePosChunkX = entityIn.chunkCoordX;
         this.frustumUpdatePosChunkY = entityIn.chunkCoordY;
         this.frustumUpdatePosChunkZ = entityIn.chunkCoordZ;
         this.viewFrustum.updateChunkPositions(entityIn.posX, entityIn.posZ);
      }

      this.world.profiler.endStartSection("renderlistcamera");
      double d3 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
      double d4 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
      double d5 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
      this.renderContainer.initialize(d3, d4, d5);
      this.world.profiler.endStartSection("cull");
      if (this.debugFixedClippingHelper != null) {
         Frustum frustum = new Frustum(this.debugFixedClippingHelper);
         frustum.setPosition(this.debugTerrainFrustumPosition.x, this.debugTerrainFrustumPosition.y, this.debugTerrainFrustumPosition.z);
         camera = frustum;
      }

      this.mc.profiler.endStartSection("culling");
      BlockPos blockpos1 = new BlockPos(d3, d4 + (double)entityIn.getEyeHeight(), d5);
      RenderChunk renderchunk = this.viewFrustum.getRenderChunk(blockpos1);
      BlockPos blockpos = new BlockPos(MathHelper.floor(d3 / 16.0D) * 16, MathHelper.floor(d4 / 16.0D) * 16, MathHelper.floor(d5 / 16.0D) * 16);
      float f = entityIn.getPitch(partialTicks);
      float f1 = entityIn.getYaw(partialTicks);
      this.displayListEntitiesDirty = this.displayListEntitiesDirty || !this.chunksToUpdate.isEmpty() || entityIn.posX != this.lastViewEntityX || entityIn.posY != this.lastViewEntityY || entityIn.posZ != this.lastViewEntityZ || (double)f != this.lastViewEntityPitch || (double)f1 != this.lastViewEntityYaw;
      this.lastViewEntityX = entityIn.posX;
      this.lastViewEntityY = entityIn.posY;
      this.lastViewEntityZ = entityIn.posZ;
      this.lastViewEntityPitch = (double)f;
      this.lastViewEntityYaw = (double)f1;
      boolean flag = this.debugFixedClippingHelper != null;
      this.mc.profiler.endStartSection("update");
      if (!flag && this.displayListEntitiesDirty) {
         this.displayListEntitiesDirty = false;
         this.renderInfos = Lists.newArrayList();
         Queue<WorldRenderer.ContainerLocalRenderInformation> queue = Queues.newArrayDeque();
         Entity.setRenderDistanceWeight(MathHelper.clamp((double)this.mc.gameSettings.renderDistanceChunks / 8.0D, 1.0D, 2.5D));
         boolean flag1 = this.mc.renderChunksMany;
         if (renderchunk != null) {
            boolean flag2 = false;
            WorldRenderer.ContainerLocalRenderInformation worldrenderer$containerlocalrenderinformation3 = new WorldRenderer.ContainerLocalRenderInformation(renderchunk, (EnumFacing)null, 0);
            Set<EnumFacing> set1 = this.getVisibleFacings(blockpos1);
            if (set1.size() == 1) {
               Vector3f vector3f = this.getViewVector(entityIn, (double)partialTicks);
               EnumFacing enumfacing = EnumFacing.getFacingFromVector(vector3f.getX(), vector3f.getY(), vector3f.getZ()).getOpposite();
               set1.remove(enumfacing);
            }

            if (set1.isEmpty()) {
               flag2 = true;
            }

            if (flag2 && !playerSpectator) {
               this.renderInfos.add(worldrenderer$containerlocalrenderinformation3);
            } else {
               if (playerSpectator && this.world.getBlockState(blockpos1).isOpaqueCube(this.world, blockpos1)) {
                  flag1 = false;
               }

               renderchunk.setFrameIndex(frameCount);
               queue.add(worldrenderer$containerlocalrenderinformation3);
            }
         } else {
            int i = blockpos1.getY() > 0 ? 248 : 8;

            for(int j = -this.renderDistanceChunks; j <= this.renderDistanceChunks; ++j) {
               for(int k = -this.renderDistanceChunks; k <= this.renderDistanceChunks; ++k) {
                  RenderChunk renderchunk1 = this.viewFrustum.getRenderChunk(new BlockPos((j << 4) + 8, i, (k << 4) + 8));
                  if (renderchunk1 != null && camera.isBoundingBoxInFrustum(renderchunk1.boundingBox.expand(0.0, blockpos1.getY() > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY, 0.0))) { // Forge: fix MC-73139
                     renderchunk1.setFrameIndex(frameCount);
                     queue.add(new WorldRenderer.ContainerLocalRenderInformation(renderchunk1, (EnumFacing)null, 0));
                  }
               }
            }
         }

         this.mc.profiler.startSection("iteration");

         while(!queue.isEmpty()) {
            WorldRenderer.ContainerLocalRenderInformation worldrenderer$containerlocalrenderinformation1 = queue.poll();
            RenderChunk renderchunk3 = worldrenderer$containerlocalrenderinformation1.renderChunk;
            EnumFacing enumfacing2 = worldrenderer$containerlocalrenderinformation1.facing;
            this.renderInfos.add(worldrenderer$containerlocalrenderinformation1);

            for(EnumFacing enumfacing1 : FACINGS) {
               RenderChunk renderchunk2 = this.getRenderChunkOffset(blockpos, renderchunk3, enumfacing1);
               if ((!flag1 || !worldrenderer$containerlocalrenderinformation1.hasDirection(enumfacing1.getOpposite())) && (!flag1 || enumfacing2 == null || renderchunk3.getCompiledChunk().isVisible(enumfacing2.getOpposite(), enumfacing1)) && renderchunk2 != null && renderchunk2.setFrameIndex(frameCount) && camera.isBoundingBoxInFrustum(renderchunk2.boundingBox)) {
                  WorldRenderer.ContainerLocalRenderInformation worldrenderer$containerlocalrenderinformation = new WorldRenderer.ContainerLocalRenderInformation(renderchunk2, enumfacing1, worldrenderer$containerlocalrenderinformation1.counter + 1);
                  worldrenderer$containerlocalrenderinformation.setDirection(worldrenderer$containerlocalrenderinformation1.setFacing, enumfacing1);
                  queue.add(worldrenderer$containerlocalrenderinformation);
               }
            }
         }

         this.mc.profiler.endSection();
      }

      this.mc.profiler.endStartSection("captureFrustum");
      if (this.debugFixTerrainFrustum) {
         this.fixTerrainFrustum(d3, d4, d5);
         this.debugFixTerrainFrustum = false;
      }

      this.mc.profiler.endStartSection("rebuildNear");
      Set<RenderChunk> set = this.chunksToUpdate;
      this.chunksToUpdate = Sets.newLinkedHashSet();

      for(WorldRenderer.ContainerLocalRenderInformation worldrenderer$containerlocalrenderinformation2 : this.renderInfos) {
         RenderChunk renderchunk4 = worldrenderer$containerlocalrenderinformation2.renderChunk;
         if (renderchunk4.needsUpdate() || set.contains(renderchunk4)) {
            this.displayListEntitiesDirty = true;
            BlockPos blockpos2 = renderchunk4.getPosition().add(8, 8, 8);
            boolean flag3 = blockpos2.distanceSq(blockpos1) < 768.0D;
            if (net.minecraftforge.common.ForgeMod.alwaysSetupTerrainOffThread || !renderchunk4.needsImmediateUpdate() && !flag3) {
               this.chunksToUpdate.add(renderchunk4);
            } else {
               this.mc.profiler.startSection("build near");
               this.renderDispatcher.updateChunkNow(renderchunk4);
               renderchunk4.clearNeedsUpdate();
               this.mc.profiler.endSection();
            }
         }
      }

      this.chunksToUpdate.addAll(set);
      this.mc.profiler.endSection();
   }

   private Set<EnumFacing> getVisibleFacings(BlockPos pos) {
      VisGraph visgraph = new VisGraph();
      BlockPos blockpos = new BlockPos(pos.getX() >> 4 << 4, pos.getY() >> 4 << 4, pos.getZ() >> 4 << 4);
      Chunk chunk = this.world.getChunk(blockpos);

      for(BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(blockpos, blockpos.add(15, 15, 15))) {
         if (chunk.getBlockState(blockpos$mutableblockpos).isOpaqueCube(this.world, blockpos$mutableblockpos)) {
            visgraph.setOpaqueCube(blockpos$mutableblockpos);
         }
      }

      return visgraph.getVisibleFacings(pos);
   }

   /**
    * Returns RenderChunk offset from given RenderChunk in given direction, or null if it can't be seen by player at
    * given BlockPos.
    */
   @Nullable
   private RenderChunk getRenderChunkOffset(BlockPos playerPos, RenderChunk renderChunkBase, EnumFacing facing) {
      BlockPos blockpos = renderChunkBase.getBlockPosOffset16(facing);
      if (MathHelper.abs(playerPos.getX() - blockpos.getX()) > this.renderDistanceChunks * 16) {
         return null;
      } else if (blockpos.getY() >= 0 && blockpos.getY() < 256) {
         return MathHelper.abs(playerPos.getZ() - blockpos.getZ()) > this.renderDistanceChunks * 16 ? null : this.viewFrustum.getRenderChunk(blockpos);
      } else {
         return null;
      }
   }

   private void fixTerrainFrustum(double x, double y, double z) {
   }

   protected Vector3f getViewVector(Entity entityIn, double partialTicks) {
      float f = (float)((double)entityIn.prevRotationPitch + (double)(entityIn.rotationPitch - entityIn.prevRotationPitch) * partialTicks);
      float f1 = (float)((double)entityIn.prevRotationYaw + (double)(entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks);
      if (Minecraft.getInstance().gameSettings.thirdPersonView == 2) {
         f += 180.0F;
      }

      float f2 = MathHelper.cos(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
      float f3 = MathHelper.sin(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
      float f4 = -MathHelper.cos(-f * ((float)Math.PI / 180F));
      float f5 = MathHelper.sin(-f * ((float)Math.PI / 180F));
      return new Vector3f(f3 * f4, f5, f2 * f4);
   }

   public int renderBlockLayer(BlockRenderLayer blockLayerIn, double partialTicks, Entity entityIn) {
      RenderHelper.disableStandardItemLighting();
      if (blockLayerIn == BlockRenderLayer.TRANSLUCENT) {
         this.mc.profiler.startSection("translucent_sort");
         double d0 = entityIn.posX - this.prevRenderSortX;
         double d1 = entityIn.posY - this.prevRenderSortY;
         double d2 = entityIn.posZ - this.prevRenderSortZ;
         if (d0 * d0 + d1 * d1 + d2 * d2 > 1.0D) {
            this.prevRenderSortX = entityIn.posX;
            this.prevRenderSortY = entityIn.posY;
            this.prevRenderSortZ = entityIn.posZ;
            int k = 0;

            for(WorldRenderer.ContainerLocalRenderInformation worldrenderer$containerlocalrenderinformation : this.renderInfos) {
               if (worldrenderer$containerlocalrenderinformation.renderChunk.compiledChunk.isLayerStarted(blockLayerIn) && k++ < 15) {
                  this.renderDispatcher.updateTransparencyLater(worldrenderer$containerlocalrenderinformation.renderChunk);
               }
            }
         }

         this.mc.profiler.endSection();
      }

      this.mc.profiler.startSection("filterempty");
      int l = 0;
      boolean flag = blockLayerIn == BlockRenderLayer.TRANSLUCENT;
      int i1 = flag ? this.renderInfos.size() - 1 : 0;
      int i = flag ? -1 : this.renderInfos.size();
      int j1 = flag ? -1 : 1;

      for(int j = i1; j != i; j += j1) {
         RenderChunk renderchunk = (this.renderInfos.get(j)).renderChunk;
         if (!renderchunk.getCompiledChunk().isLayerEmpty(blockLayerIn)) {
            ++l;
            this.renderContainer.addRenderChunk(renderchunk, blockLayerIn);
         }
      }

      this.mc.profiler.endStartSection(() -> {
         return "render_" + blockLayerIn;
      });
      this.renderBlockLayer(blockLayerIn);
      this.mc.profiler.endSection();
      return l;
   }

   private void renderBlockLayer(BlockRenderLayer blockLayerIn) {
      this.mc.gameRenderer.enableLightmap();
      if (OpenGlHelper.useVbo()) {
         GlStateManager.enableClientState(32884);
         OpenGlHelper.glClientActiveTexture(OpenGlHelper.GL_TEXTURE0);
         GlStateManager.enableClientState(32888);
         OpenGlHelper.glClientActiveTexture(OpenGlHelper.GL_TEXTURE1);
         GlStateManager.enableClientState(32888);
         OpenGlHelper.glClientActiveTexture(OpenGlHelper.GL_TEXTURE0);
         GlStateManager.enableClientState(32886);
      }

      this.renderContainer.renderChunkLayer(blockLayerIn);
      if (OpenGlHelper.useVbo()) {
         for(VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements()) {
            VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
            int i = vertexformatelement.getIndex();
            switch(vertexformatelement$enumusage) {
               case POSITION:
                  GlStateManager.disableClientState(32884);
                  break;
               case UV:
                  OpenGlHelper.glClientActiveTexture(OpenGlHelper.GL_TEXTURE0 + i);
                  GlStateManager.disableClientState(32888);
                  OpenGlHelper.glClientActiveTexture(OpenGlHelper.GL_TEXTURE0);
                  break;
               case COLOR:
                  GlStateManager.disableClientState(32886);
                  GlStateManager.resetColor();
            }
         }
      }

      this.mc.gameRenderer.disableLightmap();
   }

   private void cleanupDamagedBlocks(Iterator<DestroyBlockProgress> iteratorIn) {
      while(iteratorIn.hasNext()) {
         DestroyBlockProgress destroyblockprogress = iteratorIn.next();
         int i = destroyblockprogress.getCreationCloudUpdateTick();
         if (this.ticks - i > 400) {
            iteratorIn.remove();
         }
      }

   }

   public void tick() {
      ++this.ticks;
      if (this.ticks % 20 == 0) {
         this.cleanupDamagedBlocks(this.damagedBlocks.values().iterator());
      }

      if (!this.setLightUpdates.isEmpty() && !this.renderDispatcher.hasNoFreeRenderBuilders() && this.chunksToUpdate.isEmpty()) {
         Iterator<BlockPos> iterator = this.setLightUpdates.iterator();

         while(iterator.hasNext()) {
            BlockPos blockpos = iterator.next();
            iterator.remove();
            int i = blockpos.getX();
            int j = blockpos.getY();
            int k = blockpos.getZ();
            this.markBlocksForUpdate(i - 1, j - 1, k - 1, i + 1, j + 1, k + 1, false);
         }
      }

   }

   private void renderSkyEnd() {
      GlStateManager.disableFog();
      GlStateManager.disableAlphaTest();
      GlStateManager.enableBlend();
      GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      RenderHelper.disableStandardItemLighting();
      GlStateManager.depthMask(false);
      this.textureManager.bindTexture(END_SKY_TEXTURES);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();

      for(int i = 0; i < 6; ++i) {
         GlStateManager.pushMatrix();
         if (i == 1) {
            GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
         }

         if (i == 2) {
            GlStateManager.rotatef(-90.0F, 1.0F, 0.0F, 0.0F);
         }

         if (i == 3) {
            GlStateManager.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
         }

         if (i == 4) {
            GlStateManager.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
         }

         if (i == 5) {
            GlStateManager.rotatef(-90.0F, 0.0F, 0.0F, 1.0F);
         }

         bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
         bufferbuilder.pos(-100.0D, -100.0D, -100.0D).tex(0.0D, 0.0D).color(40, 40, 40, 255).endVertex();
         bufferbuilder.pos(-100.0D, -100.0D, 100.0D).tex(0.0D, 16.0D).color(40, 40, 40, 255).endVertex();
         bufferbuilder.pos(100.0D, -100.0D, 100.0D).tex(16.0D, 16.0D).color(40, 40, 40, 255).endVertex();
         bufferbuilder.pos(100.0D, -100.0D, -100.0D).tex(16.0D, 0.0D).color(40, 40, 40, 255).endVertex();
         tessellator.draw();
         GlStateManager.popMatrix();
      }

      GlStateManager.depthMask(true);
      GlStateManager.enableTexture2D();
      GlStateManager.enableAlphaTest();
   }

   public void renderSky(float partialTicks) {
      net.minecraftforge.client.IRenderHandler renderer = this.world.getDimension().getSkyRenderer();
      if (renderer != null) {
         renderer.render(partialTicks, world, mc);
         return;
      }
      if (this.mc.world.dimension.getType() == DimensionType.THE_END) {
         this.renderSkyEnd();
      } else if (this.mc.world.dimension.isSurfaceWorld()) {
         GlStateManager.disableTexture2D();
         Vec3d vec3d = this.world.getSkyColor(this.mc.getRenderViewEntity(), partialTicks);
         float f = (float)vec3d.x;
         float f1 = (float)vec3d.y;
         float f2 = (float)vec3d.z;
         GlStateManager.color3f(f, f1, f2);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder bufferbuilder = tessellator.getBuffer();
         GlStateManager.depthMask(false);
         GlStateManager.enableFog();
         GlStateManager.color3f(f, f1, f2);
         if (this.vboEnabled) {
            this.skyVBO.bindBuffer();
            GlStateManager.enableClientState(32884);
            GlStateManager.vertexPointer(3, 5126, 12, 0);
            this.skyVBO.drawArrays(7);
            this.skyVBO.unbindBuffer();
            GlStateManager.disableClientState(32884);
         } else {
            GlStateManager.callList(this.glSkyList);
         }

         GlStateManager.disableFog();
         GlStateManager.disableAlphaTest();
         GlStateManager.enableBlend();
         GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
         RenderHelper.disableStandardItemLighting();
         float[] afloat = this.world.dimension.calcSunriseSunsetColors(this.world.getCelestialAngle(partialTicks), partialTicks);
         if (afloat != null) {
            GlStateManager.disableTexture2D();
            GlStateManager.shadeModel(7425);
            GlStateManager.pushMatrix();
            GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotatef(MathHelper.sin(this.world.getCelestialAngleRadians(partialTicks)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
            float f3 = afloat[0];
            float f4 = afloat[1];
            float f5 = afloat[2];
            bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos(0.0D, 100.0D, 0.0D).color(f3, f4, f5, afloat[3]).endVertex();
            int i = 16;

            for(int j = 0; j <= 16; ++j) {
               float f6 = (float)j * ((float)Math.PI * 2F) / 16.0F;
               float f7 = MathHelper.sin(f6);
               float f8 = MathHelper.cos(f6);
               bufferbuilder.pos((double)(f7 * 120.0F), (double)(f8 * 120.0F), (double)(-f8 * 40.0F * afloat[3])).color(afloat[0], afloat[1], afloat[2], 0.0F).endVertex();
            }

            tessellator.draw();
            GlStateManager.popMatrix();
            GlStateManager.shadeModel(7424);
         }

         GlStateManager.enableTexture2D();
         GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
         GlStateManager.pushMatrix();
         float f11 = 1.0F - this.world.getRainStrength(partialTicks);
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, f11);
         GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotatef(this.world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
         float f12 = 30.0F;
         this.textureManager.bindTexture(SUN_TEXTURES);
         bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
         bufferbuilder.pos((double)(-f12), 100.0D, (double)(-f12)).tex(0.0D, 0.0D).endVertex();
         bufferbuilder.pos((double)f12, 100.0D, (double)(-f12)).tex(1.0D, 0.0D).endVertex();
         bufferbuilder.pos((double)f12, 100.0D, (double)f12).tex(1.0D, 1.0D).endVertex();
         bufferbuilder.pos((double)(-f12), 100.0D, (double)f12).tex(0.0D, 1.0D).endVertex();
         tessellator.draw();
         f12 = 20.0F;
         this.textureManager.bindTexture(MOON_PHASES_TEXTURES);
         int k = this.world.getMoonPhase();
         int l = k % 4;
         int i1 = k / 4 % 2;
         float f13 = (float)(l + 0) / 4.0F;
         float f14 = (float)(i1 + 0) / 2.0F;
         float f15 = (float)(l + 1) / 4.0F;
         float f9 = (float)(i1 + 1) / 2.0F;
         bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
         bufferbuilder.pos((double)(-f12), -100.0D, (double)f12).tex((double)f15, (double)f9).endVertex();
         bufferbuilder.pos((double)f12, -100.0D, (double)f12).tex((double)f13, (double)f9).endVertex();
         bufferbuilder.pos((double)f12, -100.0D, (double)(-f12)).tex((double)f13, (double)f14).endVertex();
         bufferbuilder.pos((double)(-f12), -100.0D, (double)(-f12)).tex((double)f15, (double)f14).endVertex();
         tessellator.draw();
         GlStateManager.disableTexture2D();
         float f10 = this.world.getStarBrightness(partialTicks) * f11;
         if (f10 > 0.0F) {
            GlStateManager.color4f(f10, f10, f10, f10);
            if (this.vboEnabled) {
               this.starVBO.bindBuffer();
               GlStateManager.enableClientState(32884);
               GlStateManager.vertexPointer(3, 5126, 12, 0);
               this.starVBO.drawArrays(7);
               this.starVBO.unbindBuffer();
               GlStateManager.disableClientState(32884);
            } else {
               GlStateManager.callList(this.starGLCallList);
            }
         }

         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.disableBlend();
         GlStateManager.enableAlphaTest();
         GlStateManager.enableFog();
         GlStateManager.popMatrix();
         GlStateManager.disableTexture2D();
         GlStateManager.color3f(0.0F, 0.0F, 0.0F);
         double d0 = this.mc.player.getEyePosition(partialTicks).y - this.world.getHorizon();
         if (d0 < 0.0D) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef(0.0F, 12.0F, 0.0F);
            if (this.vboEnabled) {
               this.sky2VBO.bindBuffer();
               GlStateManager.enableClientState(32884);
               GlStateManager.vertexPointer(3, 5126, 12, 0);
               this.sky2VBO.drawArrays(7);
               this.sky2VBO.unbindBuffer();
               GlStateManager.disableClientState(32884);
            } else {
               GlStateManager.callList(this.glSkyList2);
            }

            GlStateManager.popMatrix();
         }

         if (this.world.dimension.isSkyColored()) {
            GlStateManager.color3f(f * 0.2F + 0.04F, f1 * 0.2F + 0.04F, f2 * 0.6F + 0.1F);
         } else {
            GlStateManager.color3f(f, f1, f2);
         }

         GlStateManager.pushMatrix();
         GlStateManager.translatef(0.0F, -((float)(d0 - 16.0D)), 0.0F);
         GlStateManager.callList(this.glSkyList2);
         GlStateManager.popMatrix();
         GlStateManager.enableTexture2D();
         GlStateManager.depthMask(true);
      }
   }

   public void renderClouds(float partialTicks, double viewEntityX, double viewEntityY, double viewEntityZ) {
      if (net.minecraftforge.client.CloudRenderer.renderClouds(this.ticks, partialTicks, this.world, mc)) return;
      if (this.mc.world.dimension.isSurfaceWorld()) {
         float f = 12.0F;
         float f1 = 4.0F;
         double d0 = 2.0E-4D;
         double d1 = (double)(((float)this.ticks + partialTicks) * 0.03F);
         double d2 = (viewEntityX + d1) / 12.0D;
         double d3 = (double)(this.world.dimension.getCloudHeight() - (float)viewEntityY + 0.33F);
         double d4 = viewEntityZ / 12.0D + (double)0.33F;
         d2 = d2 - (double)(MathHelper.floor(d2 / 2048.0D) * 2048);
         d4 = d4 - (double)(MathHelper.floor(d4 / 2048.0D) * 2048);
         float f2 = (float)(d2 - (double)MathHelper.floor(d2));
         float f3 = (float)(d3 / 4.0D - (double)MathHelper.floor(d3 / 4.0D)) * 4.0F;
         float f4 = (float)(d4 - (double)MathHelper.floor(d4));
         Vec3d vec3d = this.world.getCloudColour(partialTicks);
         int i = (int)Math.floor(d2);
         int j = (int)Math.floor(d3 / 4.0D);
         int k = (int)Math.floor(d4);
         if (i != this.cloudsCheckX || j != this.cloudsCheckY || k != this.cloudsCheckZ || this.mc.gameSettings.shouldRenderClouds() != this.cloudRenderMode || this.cloudsCheckColor.squareDistanceTo(vec3d) > 2.0E-4D) {
            this.cloudsCheckX = i;
            this.cloudsCheckY = j;
            this.cloudsCheckZ = k;
            this.cloudsCheckColor = vec3d;
            this.cloudRenderMode = this.mc.gameSettings.shouldRenderClouds();
            this.cloudsNeedUpdate = true;
         }

         if (this.cloudsNeedUpdate) {
            this.cloudsNeedUpdate = false;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            if (this.cloudsVBO != null) {
               this.cloudsVBO.deleteGlBuffers();
            }

            if (this.glCloudsList >= 0) {
               GLAllocation.deleteDisplayLists(this.glCloudsList);
               this.glCloudsList = -1;
            }

            if (this.vboEnabled) {
               this.cloudsVBO = new VertexBuffer(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
               this.drawClouds(bufferbuilder, d2, d3, d4, vec3d);
               bufferbuilder.finishDrawing();
               bufferbuilder.reset();
               this.cloudsVBO.bufferData(bufferbuilder.getByteBuffer());
            } else {
               this.glCloudsList = GLAllocation.generateDisplayLists(1);
               GlStateManager.newList(this.glCloudsList, 4864);
               this.drawClouds(bufferbuilder, d2, d3, d4, vec3d);
               tessellator.draw();
               GlStateManager.endList();
            }
         }

         GlStateManager.disableCull();
         this.textureManager.bindTexture(CLOUDS_TEXTURES);
         GlStateManager.enableBlend();
         GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
         GlStateManager.pushMatrix();
         GlStateManager.scalef(12.0F, 1.0F, 12.0F);
         GlStateManager.translatef(-f2, f3, -f4);
         if (this.vboEnabled && this.cloudsVBO != null) {
            this.cloudsVBO.bindBuffer();
            GlStateManager.enableClientState(32884);
            GlStateManager.enableClientState(32888);
            OpenGlHelper.glClientActiveTexture(OpenGlHelper.GL_TEXTURE0);
            GlStateManager.enableClientState(32886);
            GlStateManager.enableClientState(32885);
            GlStateManager.vertexPointer(3, 5126, 28, 0);
            GlStateManager.texCoordPointer(2, 5126, 28, 12);
            GlStateManager.colorPointer(4, 5121, 28, 20);
            GlStateManager.normalPointer(5120, 28, 24);
            int i1 = this.cloudRenderMode == 2 ? 0 : 1;

            for(int k1 = i1; k1 < 2; ++k1) {
               if (k1 == 0) {
                  GlStateManager.colorMask(false, false, false, false);
               } else {
                  GlStateManager.colorMask(true, true, true, true);
               }

               this.cloudsVBO.drawArrays(7);
            }

            this.cloudsVBO.unbindBuffer();
            GlStateManager.disableClientState(32884);
            GlStateManager.disableClientState(32888);
            GlStateManager.disableClientState(32886);
            GlStateManager.disableClientState(32885);
            OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
         } else if (this.glCloudsList >= 0) {
            int l = this.cloudRenderMode == 2 ? 0 : 1;

            for(int j1 = l; j1 < 2; ++j1) {
               if (j1 == 0) {
                  GlStateManager.colorMask(false, false, false, false);
               } else {
                  GlStateManager.colorMask(true, true, true, true);
               }

               GlStateManager.callList(this.glCloudsList);
            }
         }

         GlStateManager.popMatrix();
         GlStateManager.resetColor();
         GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.disableBlend();
         GlStateManager.enableCull();
      }
   }

   private void drawClouds(BufferBuilder bufferIn, double cloudsX, double cloudsY, double cloudsZ, Vec3d cloudsColor) {
      float f = 4.0F;
      float f1 = 0.00390625F;
      int i = 8;
      int j = 4;
      float f2 = 9.765625E-4F;
      float f3 = (float)MathHelper.floor(cloudsX) * 0.00390625F;
      float f4 = (float)MathHelper.floor(cloudsZ) * 0.00390625F;
      float f5 = (float)cloudsColor.x;
      float f6 = (float)cloudsColor.y;
      float f7 = (float)cloudsColor.z;
      float f8 = f5 * 0.9F;
      float f9 = f6 * 0.9F;
      float f10 = f7 * 0.9F;
      float f11 = f5 * 0.7F;
      float f12 = f6 * 0.7F;
      float f13 = f7 * 0.7F;
      float f14 = f5 * 0.8F;
      float f15 = f6 * 0.8F;
      float f16 = f7 * 0.8F;
      bufferIn.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
      float f17 = (float)Math.floor(cloudsY / 4.0D) * 4.0F;
      if (this.cloudRenderMode == 2) {
         for(int k = -3; k <= 4; ++k) {
            for(int l = -3; l <= 4; ++l) {
               float f18 = (float)(k * 8);
               float f19 = (float)(l * 8);
               if (f17 > -5.0F) {
                  bufferIn.pos((double)(f18 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + 8.0F)).tex((double)((f18 + 0.0F) * 0.00390625F + f3), (double)((f19 + 8.0F) * 0.00390625F + f4)).color(f11, f12, f13, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                  bufferIn.pos((double)(f18 + 8.0F), (double)(f17 + 0.0F), (double)(f19 + 8.0F)).tex((double)((f18 + 8.0F) * 0.00390625F + f3), (double)((f19 + 8.0F) * 0.00390625F + f4)).color(f11, f12, f13, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                  bufferIn.pos((double)(f18 + 8.0F), (double)(f17 + 0.0F), (double)(f19 + 0.0F)).tex((double)((f18 + 8.0F) * 0.00390625F + f3), (double)((f19 + 0.0F) * 0.00390625F + f4)).color(f11, f12, f13, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                  bufferIn.pos((double)(f18 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + 0.0F)).tex((double)((f18 + 0.0F) * 0.00390625F + f3), (double)((f19 + 0.0F) * 0.00390625F + f4)).color(f11, f12, f13, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
               }

               if (f17 <= 5.0F) {
                  bufferIn.pos((double)(f18 + 0.0F), (double)(f17 + 4.0F - 9.765625E-4F), (double)(f19 + 8.0F)).tex((double)((f18 + 0.0F) * 0.00390625F + f3), (double)((f19 + 8.0F) * 0.00390625F + f4)).color(f5, f6, f7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                  bufferIn.pos((double)(f18 + 8.0F), (double)(f17 + 4.0F - 9.765625E-4F), (double)(f19 + 8.0F)).tex((double)((f18 + 8.0F) * 0.00390625F + f3), (double)((f19 + 8.0F) * 0.00390625F + f4)).color(f5, f6, f7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                  bufferIn.pos((double)(f18 + 8.0F), (double)(f17 + 4.0F - 9.765625E-4F), (double)(f19 + 0.0F)).tex((double)((f18 + 8.0F) * 0.00390625F + f3), (double)((f19 + 0.0F) * 0.00390625F + f4)).color(f5, f6, f7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                  bufferIn.pos((double)(f18 + 0.0F), (double)(f17 + 4.0F - 9.765625E-4F), (double)(f19 + 0.0F)).tex((double)((f18 + 0.0F) * 0.00390625F + f3), (double)((f19 + 0.0F) * 0.00390625F + f4)).color(f5, f6, f7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
               }

               if (k > -1) {
                  for(int i1 = 0; i1 < 8; ++i1) {
                     bufferIn.pos((double)(f18 + (float)i1 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + 8.0F)).tex((double)((f18 + (float)i1 + 0.5F) * 0.00390625F + f3), (double)((f19 + 8.0F) * 0.00390625F + f4)).color(f8, f9, f10, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                     bufferIn.pos((double)(f18 + (float)i1 + 0.0F), (double)(f17 + 4.0F), (double)(f19 + 8.0F)).tex((double)((f18 + (float)i1 + 0.5F) * 0.00390625F + f3), (double)((f19 + 8.0F) * 0.00390625F + f4)).color(f8, f9, f10, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                     bufferIn.pos((double)(f18 + (float)i1 + 0.0F), (double)(f17 + 4.0F), (double)(f19 + 0.0F)).tex((double)((f18 + (float)i1 + 0.5F) * 0.00390625F + f3), (double)((f19 + 0.0F) * 0.00390625F + f4)).color(f8, f9, f10, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                     bufferIn.pos((double)(f18 + (float)i1 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + 0.0F)).tex((double)((f18 + (float)i1 + 0.5F) * 0.00390625F + f3), (double)((f19 + 0.0F) * 0.00390625F + f4)).color(f8, f9, f10, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                  }
               }

               if (k <= 1) {
                  for(int j2 = 0; j2 < 8; ++j2) {
                     bufferIn.pos((double)(f18 + (float)j2 + 1.0F - 9.765625E-4F), (double)(f17 + 0.0F), (double)(f19 + 8.0F)).tex((double)((f18 + (float)j2 + 0.5F) * 0.00390625F + f3), (double)((f19 + 8.0F) * 0.00390625F + f4)).color(f8, f9, f10, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                     bufferIn.pos((double)(f18 + (float)j2 + 1.0F - 9.765625E-4F), (double)(f17 + 4.0F), (double)(f19 + 8.0F)).tex((double)((f18 + (float)j2 + 0.5F) * 0.00390625F + f3), (double)((f19 + 8.0F) * 0.00390625F + f4)).color(f8, f9, f10, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                     bufferIn.pos((double)(f18 + (float)j2 + 1.0F - 9.765625E-4F), (double)(f17 + 4.0F), (double)(f19 + 0.0F)).tex((double)((f18 + (float)j2 + 0.5F) * 0.00390625F + f3), (double)((f19 + 0.0F) * 0.00390625F + f4)).color(f8, f9, f10, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                     bufferIn.pos((double)(f18 + (float)j2 + 1.0F - 9.765625E-4F), (double)(f17 + 0.0F), (double)(f19 + 0.0F)).tex((double)((f18 + (float)j2 + 0.5F) * 0.00390625F + f3), (double)((f19 + 0.0F) * 0.00390625F + f4)).color(f8, f9, f10, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                  }
               }

               if (l > -1) {
                  for(int k2 = 0; k2 < 8; ++k2) {
                     bufferIn.pos((double)(f18 + 0.0F), (double)(f17 + 4.0F), (double)(f19 + (float)k2 + 0.0F)).tex((double)((f18 + 0.0F) * 0.00390625F + f3), (double)((f19 + (float)k2 + 0.5F) * 0.00390625F + f4)).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                     bufferIn.pos((double)(f18 + 8.0F), (double)(f17 + 4.0F), (double)(f19 + (float)k2 + 0.0F)).tex((double)((f18 + 8.0F) * 0.00390625F + f3), (double)((f19 + (float)k2 + 0.5F) * 0.00390625F + f4)).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                     bufferIn.pos((double)(f18 + 8.0F), (double)(f17 + 0.0F), (double)(f19 + (float)k2 + 0.0F)).tex((double)((f18 + 8.0F) * 0.00390625F + f3), (double)((f19 + (float)k2 + 0.5F) * 0.00390625F + f4)).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                     bufferIn.pos((double)(f18 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + (float)k2 + 0.0F)).tex((double)((f18 + 0.0F) * 0.00390625F + f3), (double)((f19 + (float)k2 + 0.5F) * 0.00390625F + f4)).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                  }
               }

               if (l <= 1) {
                  for(int l2 = 0; l2 < 8; ++l2) {
                     bufferIn.pos((double)(f18 + 0.0F), (double)(f17 + 4.0F), (double)(f19 + (float)l2 + 1.0F - 9.765625E-4F)).tex((double)((f18 + 0.0F) * 0.00390625F + f3), (double)((f19 + (float)l2 + 0.5F) * 0.00390625F + f4)).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                     bufferIn.pos((double)(f18 + 8.0F), (double)(f17 + 4.0F), (double)(f19 + (float)l2 + 1.0F - 9.765625E-4F)).tex((double)((f18 + 8.0F) * 0.00390625F + f3), (double)((f19 + (float)l2 + 0.5F) * 0.00390625F + f4)).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                     bufferIn.pos((double)(f18 + 8.0F), (double)(f17 + 0.0F), (double)(f19 + (float)l2 + 1.0F - 9.765625E-4F)).tex((double)((f18 + 8.0F) * 0.00390625F + f3), (double)((f19 + (float)l2 + 0.5F) * 0.00390625F + f4)).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                     bufferIn.pos((double)(f18 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + (float)l2 + 1.0F - 9.765625E-4F)).tex((double)((f18 + 0.0F) * 0.00390625F + f3), (double)((f19 + (float)l2 + 0.5F) * 0.00390625F + f4)).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                  }
               }
            }
         }
      } else {
         int j1 = 1;
         int k1 = 32;

         for(int l1 = -32; l1 < 32; l1 += 32) {
            for(int i2 = -32; i2 < 32; i2 += 32) {
               bufferIn.pos((double)(l1 + 0), (double)f17, (double)(i2 + 32)).tex((double)((float)(l1 + 0) * 0.00390625F + f3), (double)((float)(i2 + 32) * 0.00390625F + f4)).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
               bufferIn.pos((double)(l1 + 32), (double)f17, (double)(i2 + 32)).tex((double)((float)(l1 + 32) * 0.00390625F + f3), (double)((float)(i2 + 32) * 0.00390625F + f4)).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
               bufferIn.pos((double)(l1 + 32), (double)f17, (double)(i2 + 0)).tex((double)((float)(l1 + 32) * 0.00390625F + f3), (double)((float)(i2 + 0) * 0.00390625F + f4)).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
               bufferIn.pos((double)(l1 + 0), (double)f17, (double)(i2 + 0)).tex((double)((float)(l1 + 0) * 0.00390625F + f3), (double)((float)(i2 + 0) * 0.00390625F + f4)).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
            }
         }
      }

   }

   public void updateChunks(long finishTimeNano) {
      this.displayListEntitiesDirty |= this.renderDispatcher.runChunkUploads(finishTimeNano);
      if (!this.chunksToUpdate.isEmpty()) {
         Iterator<RenderChunk> iterator = this.chunksToUpdate.iterator();

         while(iterator.hasNext()) {
            RenderChunk renderchunk = iterator.next();
            boolean flag;
            if (renderchunk.needsImmediateUpdate()) {
               flag = this.renderDispatcher.updateChunkNow(renderchunk);
            } else {
               flag = this.renderDispatcher.updateChunkLater(renderchunk);
            }

            if (!flag) {
               break;
            }

            renderchunk.clearNeedsUpdate();
            iterator.remove();
            long i = finishTimeNano - Util.nanoTime();
            if (i < 0L) {
               break;
            }
         }
      }

   }

   public void renderWorldBorder(Entity entityIn, float partialTicks) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      WorldBorder worldborder = this.world.getWorldBorder();
      double d0 = (double)(this.mc.gameSettings.renderDistanceChunks * 16);
      if (!(entityIn.posX < worldborder.maxX() - d0) || !(entityIn.posX > worldborder.minX() + d0) || !(entityIn.posZ < worldborder.maxZ() - d0) || !(entityIn.posZ > worldborder.minZ() + d0)) {
         double d1 = 1.0D - worldborder.getClosestDistance(entityIn) / d0;
         d1 = Math.pow(d1, 4.0D);
         double d2 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
         double d3 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
         double d4 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
         GlStateManager.enableBlend();
         GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
         this.textureManager.bindTexture(FORCEFIELD_TEXTURES);
         GlStateManager.depthMask(false);
         GlStateManager.pushMatrix();
         int i = worldborder.getStatus().getColor();
         float f = (float)(i >> 16 & 255) / 255.0F;
         float f1 = (float)(i >> 8 & 255) / 255.0F;
         float f2 = (float)(i & 255) / 255.0F;
         GlStateManager.color4f(f, f1, f2, (float)d1);
         GlStateManager.polygonOffset(-3.0F, -3.0F);
         GlStateManager.enablePolygonOffset();
         GlStateManager.alphaFunc(516, 0.1F);
         GlStateManager.enableAlphaTest();
         GlStateManager.disableCull();
         float f3 = (float)(Util.milliTime() % 3000L) / 3000.0F;
         float f4 = 0.0F;
         float f5 = 0.0F;
         float f6 = 128.0F;
         bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
         bufferbuilder.setTranslation(-d2, -d3, -d4);
         double d5 = Math.max((double)MathHelper.floor(d4 - d0), worldborder.minZ());
         double d6 = Math.min((double)MathHelper.ceil(d4 + d0), worldborder.maxZ());
         if (d2 > worldborder.maxX() - d0) {
            float f7 = 0.0F;

            for(double d7 = d5; d7 < d6; f7 += 0.5F) {
               double d8 = Math.min(1.0D, d6 - d7);
               float f8 = (float)d8 * 0.5F;
               bufferbuilder.pos(worldborder.maxX(), 256.0D, d7).tex((double)(f3 + f7), (double)(f3 + 0.0F)).endVertex();
               bufferbuilder.pos(worldborder.maxX(), 256.0D, d7 + d8).tex((double)(f3 + f8 + f7), (double)(f3 + 0.0F)).endVertex();
               bufferbuilder.pos(worldborder.maxX(), 0.0D, d7 + d8).tex((double)(f3 + f8 + f7), (double)(f3 + 128.0F)).endVertex();
               bufferbuilder.pos(worldborder.maxX(), 0.0D, d7).tex((double)(f3 + f7), (double)(f3 + 128.0F)).endVertex();
               ++d7;
            }
         }

         if (d2 < worldborder.minX() + d0) {
            float f9 = 0.0F;

            for(double d9 = d5; d9 < d6; f9 += 0.5F) {
               double d12 = Math.min(1.0D, d6 - d9);
               float f12 = (float)d12 * 0.5F;
               bufferbuilder.pos(worldborder.minX(), 256.0D, d9).tex((double)(f3 + f9), (double)(f3 + 0.0F)).endVertex();
               bufferbuilder.pos(worldborder.minX(), 256.0D, d9 + d12).tex((double)(f3 + f12 + f9), (double)(f3 + 0.0F)).endVertex();
               bufferbuilder.pos(worldborder.minX(), 0.0D, d9 + d12).tex((double)(f3 + f12 + f9), (double)(f3 + 128.0F)).endVertex();
               bufferbuilder.pos(worldborder.minX(), 0.0D, d9).tex((double)(f3 + f9), (double)(f3 + 128.0F)).endVertex();
               ++d9;
            }
         }

         d5 = Math.max((double)MathHelper.floor(d2 - d0), worldborder.minX());
         d6 = Math.min((double)MathHelper.ceil(d2 + d0), worldborder.maxX());
         if (d4 > worldborder.maxZ() - d0) {
            float f10 = 0.0F;

            for(double d10 = d5; d10 < d6; f10 += 0.5F) {
               double d13 = Math.min(1.0D, d6 - d10);
               float f13 = (float)d13 * 0.5F;
               bufferbuilder.pos(d10, 256.0D, worldborder.maxZ()).tex((double)(f3 + f10), (double)(f3 + 0.0F)).endVertex();
               bufferbuilder.pos(d10 + d13, 256.0D, worldborder.maxZ()).tex((double)(f3 + f13 + f10), (double)(f3 + 0.0F)).endVertex();
               bufferbuilder.pos(d10 + d13, 0.0D, worldborder.maxZ()).tex((double)(f3 + f13 + f10), (double)(f3 + 128.0F)).endVertex();
               bufferbuilder.pos(d10, 0.0D, worldborder.maxZ()).tex((double)(f3 + f10), (double)(f3 + 128.0F)).endVertex();
               ++d10;
            }
         }

         if (d4 < worldborder.minZ() + d0) {
            float f11 = 0.0F;

            for(double d11 = d5; d11 < d6; f11 += 0.5F) {
               double d14 = Math.min(1.0D, d6 - d11);
               float f14 = (float)d14 * 0.5F;
               bufferbuilder.pos(d11, 256.0D, worldborder.minZ()).tex((double)(f3 + f11), (double)(f3 + 0.0F)).endVertex();
               bufferbuilder.pos(d11 + d14, 256.0D, worldborder.minZ()).tex((double)(f3 + f14 + f11), (double)(f3 + 0.0F)).endVertex();
               bufferbuilder.pos(d11 + d14, 0.0D, worldborder.minZ()).tex((double)(f3 + f14 + f11), (double)(f3 + 128.0F)).endVertex();
               bufferbuilder.pos(d11, 0.0D, worldborder.minZ()).tex((double)(f3 + f11), (double)(f3 + 128.0F)).endVertex();
               ++d11;
            }
         }

         tessellator.draw();
         bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);
         GlStateManager.enableCull();
         GlStateManager.disableAlphaTest();
         GlStateManager.polygonOffset(0.0F, 0.0F);
         GlStateManager.disablePolygonOffset();
         GlStateManager.enableAlphaTest();
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
         GlStateManager.depthMask(true);
      }
   }

   private void preRenderDamagedBlocks() {
      GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      GlStateManager.enableBlend();
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 0.5F);
      GlStateManager.polygonOffset(-1.0F, -10.0F);
      GlStateManager.enablePolygonOffset();
      GlStateManager.alphaFunc(516, 0.1F);
      GlStateManager.enableAlphaTest();
      GlStateManager.pushMatrix();
   }

   private void postRenderDamagedBlocks() {
      GlStateManager.disableAlphaTest();
      GlStateManager.polygonOffset(0.0F, 0.0F);
      GlStateManager.disablePolygonOffset();
      GlStateManager.enableAlphaTest();
      GlStateManager.depthMask(true);
      GlStateManager.popMatrix();
   }

   public void drawBlockDamageTexture(Tessellator tessellatorIn, BufferBuilder bufferBuilderIn, Entity entityIn, float partialTicks) {
      double d0 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
      double d1 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
      double d2 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
      if (!this.damagedBlocks.isEmpty()) {
         this.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
         this.preRenderDamagedBlocks();
         bufferBuilderIn.begin(7, DefaultVertexFormats.BLOCK);
         bufferBuilderIn.setTranslation(-d0, -d1, -d2);
         bufferBuilderIn.noColor();
         Iterator<DestroyBlockProgress> iterator = this.damagedBlocks.values().iterator();

         while(iterator.hasNext()) {
            DestroyBlockProgress destroyblockprogress = iterator.next();
            BlockPos blockpos = destroyblockprogress.getPosition();
            Block block = this.world.getBlockState(blockpos).getBlock();
            TileEntity te = this.world.getTileEntity(blockpos);
            boolean hasBreak = block instanceof BlockChest || block instanceof BlockEnderChest || block instanceof BlockSign || block instanceof BlockAbstractSkull;
            if (!hasBreak) hasBreak = te != null && te.canRenderBreaking();

            if (!hasBreak) {
               double d3 = (double)blockpos.getX() - d0;
               double d4 = (double)blockpos.getY() - d1;
               double d5 = (double)blockpos.getZ() - d2;
               if (d3 * d3 + d4 * d4 + d5 * d5 > 1024.0D) {
                  iterator.remove();
               } else {
                  IBlockState iblockstate = this.world.getBlockState(blockpos);
                  if (!iblockstate.isAir(world, blockpos)) {
                     int i = destroyblockprogress.getPartialBlockDamage();
                     TextureAtlasSprite textureatlassprite = this.destroyBlockIcons[i];
                     BlockRendererDispatcher blockrendererdispatcher = this.mc.getBlockRendererDispatcher();
                     // NoCubes Start
                     if(io.github.cadiboo.nocubes.hooks.Hooks.renderBlockDamage(tessellatorIn, bufferBuilderIn, blockpos, iblockstate, this.world, textureatlassprite, blockrendererdispatcher))
                     // NoCubes End
                     blockrendererdispatcher.renderBlockDamage(iblockstate, blockpos, textureatlassprite, this.world);
                  }
               }
            }
         }

         tessellatorIn.draw();
         bufferBuilderIn.setTranslation(0.0D, 0.0D, 0.0D);
         this.postRenderDamagedBlocks();
      }

   }

   /**
    * Draws the selection box for the player.
    */
   public void drawSelectionBox(EntityPlayer player, RayTraceResult movingObjectPositionIn, int execute, float partialTicks) {
      if (execute == 0 && movingObjectPositionIn.type == RayTraceResult.Type.BLOCK) {
         BlockPos blockpos = movingObjectPositionIn.getBlockPos();
         IBlockState iblockstate = this.world.getBlockState(blockpos);
         if (!iblockstate.isAir(world, blockpos) && this.world.getWorldBorder().contains(blockpos)) {
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.lineWidth(Math.max(2.5F, (float)this.mc.mainWindow.getFramebufferWidth() / 1920.0F * 2.5F));
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GlStateManager.matrixMode(5889);
            GlStateManager.pushMatrix();
            GlStateManager.scalef(1.0F, 1.0F, 0.999F);
            double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
            double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
            double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
            drawShape(iblockstate.getShape(this.world, blockpos), (double)blockpos.getX() - d0, (double)blockpos.getY() - d1, (double)blockpos.getZ() - d2, 0.0F, 0.0F, 0.0F, 0.4F);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
         }
      }

   }

   public static void drawVoxelShapeParts(VoxelShape voxelShapeIn, double xIn, double yIn, double zIn, float red, float green, float blue, float alpha) {
      List<AxisAlignedBB> list = voxelShapeIn.toBoundingBoxList();
      int i = MathHelper.ceil((double)list.size() / 3.0D);

      for(int j = 0; j < list.size(); ++j) {
         AxisAlignedBB axisalignedbb = list.get(j);
         float f = ((float)j % (float)i + 1.0F) / (float)i;
         float f1 = (float)(j / i);
         float f2 = f * (float)(f1 == 0.0F ? 1 : 0);
         float f3 = f * (float)(f1 == 1.0F ? 1 : 0);
         float f4 = f * (float)(f1 == 2.0F ? 1 : 0);
         drawShape(VoxelShapes.create(axisalignedbb.offset(0.0D, 0.0D, 0.0D)), xIn, yIn, zIn, f2, f3, f4, 1.0F);
      }

   }

   public static void drawShape(VoxelShape voxelShapeIn, double xIn, double yIn, double zIn, float red, float green, float blue, float alpha) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(1, DefaultVertexFormats.POSITION_COLOR);
      voxelShapeIn.forEachEdge((p_195468_11_, p_195468_13_, p_195468_15_, p_195468_17_, p_195468_19_, p_195468_21_) -> {
         bufferbuilder.pos(p_195468_11_ + xIn, p_195468_13_ + yIn, p_195468_15_ + zIn).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(p_195468_17_ + xIn, p_195468_19_ + yIn, p_195468_21_ + zIn).color(red, green, blue, alpha).endVertex();
      });
      tessellator.draw();
   }

   public static void drawSelectionBoundingBox(AxisAlignedBB box, float red, float green, float blue, float alpha) {
      drawBoundingBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha);
   }

   public static void drawBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float red, float green, float blue, float alpha) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      drawBoundingBox(bufferbuilder, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
      tessellator.draw();
   }

   public static void drawBoundingBox(BufferBuilder buffer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float red, float green, float blue, float alpha) {
      buffer.pos(minX, minY, minZ).color(red, green, blue, 0.0F).endVertex();
      buffer.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
      buffer.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
      buffer.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
      buffer.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
      buffer.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
      buffer.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
      buffer.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
      buffer.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
      buffer.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
      buffer.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
      buffer.pos(minX, maxY, maxZ).color(red, green, blue, 0.0F).endVertex();
      buffer.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
      buffer.pos(maxX, maxY, maxZ).color(red, green, blue, 0.0F).endVertex();
      buffer.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
      buffer.pos(maxX, maxY, minZ).color(red, green, blue, 0.0F).endVertex();
      buffer.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
      buffer.pos(maxX, minY, minZ).color(red, green, blue, 0.0F).endVertex();
   }

   public static void renderFilledBox(AxisAlignedBB aabb, float red, float green, float blue, float alpha) {
      renderFilledBox(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, red, green, blue, alpha);
   }

   public static void renderFilledBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float red, float green, float blue, float alpha) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);
      addChainedFilledBoxVertices(bufferbuilder, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
      tessellator.draw();
   }

   public static void addChainedFilledBoxVertices(BufferBuilder builder, double x1, double y1, double z1, double x2, double y2, double z2, float red, float green, float blue, float alpha) {
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
   }

   public void markBlocksForUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean updateImmediately) {
      this.viewFrustum.markBlocksForUpdate(minX, minY, minZ, maxX, maxY, maxZ, updateImmediately);
   }

   public void notifyBlockUpdate(IBlockReader worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      this.markBlocksForUpdate(i - 1, j - 1, k - 1, i + 1, j + 1, k + 1, (flags & 8) != 0);
   }

   public void notifyLightSet(BlockPos pos) {
      this.setLightUpdates.add(pos.toImmutable());
   }

   /**
    * On the client, re-renders all blocks in this range, inclusive. On the server, does nothing.
    */
   public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
      this.markBlocksForUpdate(x1 - 1, y1 - 1, z1 - 1, x2 + 1, y2 + 1, z2 + 1, false);
   }

   public void playRecord(@Nullable SoundEvent soundIn, BlockPos pos) {
      ISound isound = this.mapSoundPositions.get(pos);
      if (isound != null) {
         this.mc.getSoundHandler().stop(isound);
         this.mapSoundPositions.remove(pos);
      }

      if (soundIn != null) {
         ItemRecord itemrecord = ItemRecord.getBySound(soundIn);
         if (itemrecord != null) {
            this.mc.ingameGUI.setRecordPlayingMessage(itemrecord.getRecordDescription().getFormattedText());
         }

         ISound simplesound = SimpleSound.record(soundIn, (float)pos.getX(), (float)pos.getY(), (float)pos.getZ());
         this.mapSoundPositions.put(pos, simplesound);
         this.mc.getSoundHandler().play(simplesound);
      }

      this.setPartying(this.world, pos, soundIn != null);
   }

   /**
    * Called when a record starts or stops playing. Used to make parrots start or stop partying.
    */
   private void setPartying(World worldIn, BlockPos pos, boolean isPartying) {
      for(EntityLivingBase entitylivingbase : worldIn.getEntitiesWithinAABB(EntityLivingBase.class, (new AxisAlignedBB(pos)).grow(3.0D))) {
         entitylivingbase.setPartying(pos, isPartying);
      }

   }

   public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {
   }

   public void addParticle(IParticleData particleData, boolean alwaysRender, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
      this.addParticle(particleData, alwaysRender, false, x, y, z, xSpeed, ySpeed, zSpeed);
   }

   public void addParticle(IParticleData particleData, boolean ignoreRange, boolean minimizeLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
      try {
         this.addParticleUnchecked(particleData, ignoreRange, minimizeLevel, x, y, z, xSpeed, ySpeed, zSpeed);
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception while adding particle");
         CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being added");
         crashreportcategory.addDetail("ID", particleData.getType().getId());
         crashreportcategory.addDetail("Parameters", particleData.getParameters());
         crashreportcategory.addDetail("Position", () -> {
            return CrashReportCategory.getCoordinateInfo(x, y, z);
         });
         throw new ReportedException(crashreport);
      }
   }

   private <T extends IParticleData> void addParticleUnchecked(T particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
      this.addParticle(particleData, particleData.getType().getAlwaysShow(), x, y, z, xSpeed, ySpeed, zSpeed);
   }

   @Nullable
   private Particle addParticleUnchecked(IParticleData particleData, boolean alwaysRender, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
      return this.addParticleUnchecked(particleData, alwaysRender, false, x, y, z, xSpeed, ySpeed, zSpeed);
   }

   @Nullable
   private Particle addParticleUnchecked(IParticleData particleData, boolean alwaysRender, boolean minimizeLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
      Entity entity = this.mc.getRenderViewEntity();
      if (this.mc != null && entity != null && this.mc.particles != null) {
         int i = this.calculateParticleLevel(minimizeLevel);
         double d0 = entity.posX - x;
         double d1 = entity.posY - y;
         double d2 = entity.posZ - z;
         if (alwaysRender) {
            return this.mc.particles.addParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
         } else if (d0 * d0 + d1 * d1 + d2 * d2 > 1024.0D) {
            return null;
         } else {
            return i > 1 ? null : this.mc.particles.addParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
         }
      } else {
         return null;
      }
   }

   private int calculateParticleLevel(boolean minimiseLevel) {
      int i = this.mc.gameSettings.particleSetting;
      if (minimiseLevel && i == 2 && this.world.rand.nextInt(10) == 0) {
         i = 1;
      }

      if (i == 1 && this.world.rand.nextInt(3) == 0) {
         i = 2;
      }

      return i;
   }

   /**
    * Called on all IWorldAccesses when an entity is created or loaded. On client worlds, starts downloading any
    * necessary textures. On server worlds, adds the entity to the entity tracker.
    */
   public void onEntityAdded(Entity entityIn) {
   }

   /**
    * Called on all IWorldAccesses when an entity is unloaded or destroyed. On client worlds, releases any downloaded
    * textures. On server worlds, removes the entity from the entity tracker.
    */
   public void onEntityRemoved(Entity entityIn) {
   }

   /**
    * Deletes all display lists
    */
   public void deleteAllDisplayLists() {
   }

   public void broadcastSound(int soundID, BlockPos pos, int data) {
      switch(soundID) {
         case 1023:
         case 1028:
         case 1038:
            Entity entity = this.mc.getRenderViewEntity();
            if (entity != null) {
               double d0 = (double)pos.getX() - entity.posX;
               double d1 = (double)pos.getY() - entity.posY;
               double d2 = (double)pos.getZ() - entity.posZ;
               double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
               double d4 = entity.posX;
               double d5 = entity.posY;
               double d6 = entity.posZ;
               if (d3 > 0.0D) {
                  d4 += d0 / d3 * 2.0D;
                  d5 += d1 / d3 * 2.0D;
                  d6 += d2 / d3 * 2.0D;
               }

               if (soundID == 1023) {
                  this.world.playSound(d4, d5, d6, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.0F, 1.0F, false);
               } else if (soundID == 1038) {
                  this.world.playSound(d4, d5, d6, SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.HOSTILE, 1.0F, 1.0F, false);
               } else {
                  this.world.playSound(d4, d5, d6, SoundEvents.ENTITY_ENDER_DRAGON_DEATH, SoundCategory.HOSTILE, 5.0F, 1.0F, false);
               }
            }
         default:
      }
   }

   public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {
      Random random = this.world.rand;
      switch(type) {
         case 1000:
            this.world.playSound(blockPosIn, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            break;
         case 1001:
            this.world.playSound(blockPosIn, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0F, 1.2F, false);
            break;
         case 1002:
            this.world.playSound(blockPosIn, SoundEvents.BLOCK_DISPENSER_LAUNCH, SoundCategory.BLOCKS, 1.0F, 1.2F, false);
            break;
         case 1003:
            this.world.playSound(blockPosIn, SoundEvents.ENTITY_ENDER_EYE_LAUNCH, SoundCategory.NEUTRAL, 1.0F, 1.2F, false);
            break;
         case 1004:
            this.world.playSound(blockPosIn, SoundEvents.ENTITY_FIREWORK_ROCKET_SHOOT, SoundCategory.NEUTRAL, 1.0F, 1.2F, false);
            break;
         case 1005:
            this.world.playSound(blockPosIn, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1006:
            this.world.playSound(blockPosIn, SoundEvents.BLOCK_WOODEN_DOOR_OPEN, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1007:
            this.world.playSound(blockPosIn, SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1008:
            this.world.playSound(blockPosIn, SoundEvents.BLOCK_FENCE_GATE_OPEN, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1009:
            this.world.playSound(blockPosIn, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F, false);
            break;
         case 1010:
            if (Item.getItemById(data) instanceof ItemRecord) {
               this.world.playRecord(blockPosIn, ((ItemRecord)Item.getItemById(data)).getSound());
            } else {
               this.world.playRecord(blockPosIn, (SoundEvent)null);
            }
            break;
         case 1011:
            this.world.playSound(blockPosIn, SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1012:
            this.world.playSound(blockPosIn, SoundEvents.BLOCK_WOODEN_DOOR_CLOSE, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1013:
            this.world.playSound(blockPosIn, SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1014:
            this.world.playSound(blockPosIn, SoundEvents.BLOCK_FENCE_GATE_CLOSE, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1015:
            this.world.playSound(blockPosIn, SoundEvents.ENTITY_GHAST_WARN, SoundCategory.HOSTILE, 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1016:
            this.world.playSound(blockPosIn, SoundEvents.ENTITY_GHAST_SHOOT, SoundCategory.HOSTILE, 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1017:
            this.world.playSound(blockPosIn, SoundEvents.ENTITY_ENDER_DRAGON_SHOOT, SoundCategory.HOSTILE, 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1018:
            this.world.playSound(blockPosIn, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1019:
            this.world.playSound(blockPosIn, SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1020:
            this.world.playSound(blockPosIn, SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1021:
            this.world.playSound(blockPosIn, SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, SoundCategory.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1022:
            this.world.playSound(blockPosIn, SoundEvents.ENTITY_WITHER_BREAK_BLOCK, SoundCategory.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1024:
            this.world.playSound(blockPosIn, SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1025:
            this.world.playSound(blockPosIn, SoundEvents.ENTITY_BAT_TAKEOFF, SoundCategory.NEUTRAL, 0.05F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1026:
            this.world.playSound(blockPosIn, SoundEvents.ENTITY_ZOMBIE_INFECT, SoundCategory.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1027:
            this.world.playSound(blockPosIn, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, SoundCategory.NEUTRAL, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1029:
            this.world.playSound(blockPosIn, SoundEvents.BLOCK_ANVIL_DESTROY, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1030:
            this.world.playSound(blockPosIn, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1031:
            this.world.playSound(blockPosIn, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 0.3F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1032:
            this.mc.getSoundHandler().play(SimpleSound.master(SoundEvents.BLOCK_PORTAL_TRAVEL, random.nextFloat() * 0.4F + 0.8F));
            break;
         case 1033:
            this.world.playSound(blockPosIn, SoundEvents.BLOCK_CHORUS_FLOWER_GROW, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            break;
         case 1034:
            this.world.playSound(blockPosIn, SoundEvents.BLOCK_CHORUS_FLOWER_DEATH, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            break;
         case 1035:
            this.world.playSound(blockPosIn, SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            break;
         case 1036:
            this.world.playSound(blockPosIn, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1037:
            this.world.playSound(blockPosIn, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1039:
            this.world.playSound(blockPosIn, SoundEvents.ENTITY_PHANTOM_BITE, SoundCategory.HOSTILE, 0.3F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1040:
            this.world.playSound(blockPosIn, SoundEvents.ENTITY_ZOMBIE_CONVERTED_TO_DROWNED, SoundCategory.NEUTRAL, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1041:
            this.world.playSound(blockPosIn, SoundEvents.ENTITY_HUSK_CONVERTED_TO_ZOMBIE, SoundCategory.NEUTRAL, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 2000:
            EnumFacing enumfacing = EnumFacing.byIndex(data);
            int i = enumfacing.getXOffset();
            int j1 = enumfacing.getYOffset();
            int j = enumfacing.getZOffset();
            double d13 = (double)blockPosIn.getX() + (double)i * 0.6D + 0.5D;
            double d15 = (double)blockPosIn.getY() + (double)j1 * 0.6D + 0.5D;
            double d16 = (double)blockPosIn.getZ() + (double)j * 0.6D + 0.5D;

            for(int l1 = 0; l1 < 10; ++l1) {
               double d18 = random.nextDouble() * 0.2D + 0.01D;
               double d21 = d13 + (double)i * 0.01D + (random.nextDouble() - 0.5D) * (double)j * 0.5D;
               double d23 = d15 + (double)j1 * 0.01D + (random.nextDouble() - 0.5D) * (double)j1 * 0.5D;
               double d25 = d16 + (double)j * 0.01D + (random.nextDouble() - 0.5D) * (double)i * 0.5D;
               double d26 = (double)i * d18 + random.nextGaussian() * 0.01D;
               double d27 = (double)j1 * d18 + random.nextGaussian() * 0.01D;
               double d9 = (double)j * d18 + random.nextGaussian() * 0.01D;
               this.addParticleUnchecked(Particles.SMOKE, d21, d23, d25, d26, d27, d9);
            }
            break;
         case 2001:
            IBlockState iblockstate = Block.getStateById(data);
            if (!iblockstate.isAir(world, blockPosIn)) {
               SoundType soundtype = iblockstate.getSoundType(world, blockPosIn, null);
               this.world.playSound(blockPosIn, soundtype.getBreakSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F, false);
            }

            this.mc.particles.addBlockDestroyEffects(blockPosIn, iblockstate);
            break;
         case 2002:
         case 2007:
            double d10 = (double)blockPosIn.getX();
            double d11 = (double)blockPosIn.getY();
            double d12 = (double)blockPosIn.getZ();

            for(int k1 = 0; k1 < 8; ++k1) {
               this.addParticleUnchecked(new ItemParticleData(Particles.ITEM, new ItemStack(Items.SPLASH_POTION)), d10, d11, d12, random.nextGaussian() * 0.15D, random.nextDouble() * 0.2D, random.nextGaussian() * 0.15D);
            }

            float f5 = (float)(data >> 16 & 255) / 255.0F;
            float f = (float)(data >> 8 & 255) / 255.0F;
            float f1 = (float)(data >> 0 & 255) / 255.0F;
            IParticleData iparticledata = type == 2007 ? Particles.INSTANT_EFFECT : Particles.EFFECT;

            for(int l = 0; l < 100; ++l) {
               double d17 = random.nextDouble() * 4.0D;
               double d20 = random.nextDouble() * Math.PI * 2.0D;
               double d4 = Math.cos(d20) * d17;
               double d6 = 0.01D + random.nextDouble() * 0.5D;
               double d8 = Math.sin(d20) * d17;
               Particle particle1 = this.addParticleUnchecked(iparticledata, iparticledata.getType().getAlwaysShow(), d10 + d4 * 0.1D, d11 + 0.3D, d12 + d8 * 0.1D, d4, d6, d8);
               if (particle1 != null) {
                  float f4 = 0.75F + random.nextFloat() * 0.25F;
                  particle1.setColor(f5 * f4, f * f4, f1 * f4);
                  particle1.multiplyVelocity((float)d17);
               }
            }

            this.world.playSound(blockPosIn, SoundEvents.ENTITY_SPLASH_POTION_BREAK, SoundCategory.NEUTRAL, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 2003:
            double d0 = (double)blockPosIn.getX() + 0.5D;
            double d1 = (double)blockPosIn.getY();
            double d2 = (double)blockPosIn.getZ() + 0.5D;

            for(int k = 0; k < 8; ++k) {
               this.addParticleUnchecked(new ItemParticleData(Particles.ITEM, new ItemStack(Items.ENDER_EYE)), d0, d1, d2, random.nextGaussian() * 0.15D, random.nextDouble() * 0.2D, random.nextGaussian() * 0.15D);
            }

            for(double d14 = 0.0D; d14 < (Math.PI * 2D); d14 += 0.15707963267948966D) {
               this.addParticleUnchecked(Particles.PORTAL, d0 + Math.cos(d14) * 5.0D, d1 - 0.4D, d2 + Math.sin(d14) * 5.0D, Math.cos(d14) * -5.0D, 0.0D, Math.sin(d14) * -5.0D);
               this.addParticleUnchecked(Particles.PORTAL, d0 + Math.cos(d14) * 5.0D, d1 - 0.4D, d2 + Math.sin(d14) * 5.0D, Math.cos(d14) * -7.0D, 0.0D, Math.sin(d14) * -7.0D);
            }
            break;
         case 2004:
            for(int i2 = 0; i2 < 20; ++i2) {
               double d19 = (double)blockPosIn.getX() + 0.5D + ((double)this.world.rand.nextFloat() - 0.5D) * 2.0D;
               double d22 = (double)blockPosIn.getY() + 0.5D + ((double)this.world.rand.nextFloat() - 0.5D) * 2.0D;
               double d24 = (double)blockPosIn.getZ() + 0.5D + ((double)this.world.rand.nextFloat() - 0.5D) * 2.0D;
               this.world.addParticle(Particles.SMOKE, d19, d22, d24, 0.0D, 0.0D, 0.0D);
               this.world.addParticle(Particles.FLAME, d19, d22, d24, 0.0D, 0.0D, 0.0D);
            }
            break;
         case 2005:
            ItemBoneMeal.spawnBonemealParticles(this.world, blockPosIn, data);
            break;
         case 2006:
            for(int i1 = 0; i1 < 200; ++i1) {
               float f2 = random.nextFloat() * 4.0F;
               float f3 = random.nextFloat() * ((float)Math.PI * 2F);
               double d3 = (double)(MathHelper.cos(f3) * f2);
               double d5 = 0.01D + random.nextDouble() * 0.5D;
               double d7 = (double)(MathHelper.sin(f3) * f2);
               Particle particle = this.addParticleUnchecked(Particles.DRAGON_BREATH, false, (double)blockPosIn.getX() + d3 * 0.1D, (double)blockPosIn.getY() + 0.3D, (double)blockPosIn.getZ() + d7 * 0.1D, d3, d5, d7);
               if (particle != null) {
                  particle.multiplyVelocity(f2);
               }
            }

            this.world.playSound(blockPosIn, SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.HOSTILE, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 3000:
            this.world.addParticle(Particles.EXPLOSION_EMITTER, true, (double)blockPosIn.getX() + 0.5D, (double)blockPosIn.getY() + 0.5D, (double)blockPosIn.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
            this.world.playSound(blockPosIn, SoundEvents.BLOCK_END_GATEWAY_SPAWN, SoundCategory.BLOCKS, 10.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F, false);
            break;
         case 3001:
            this.world.playSound(blockPosIn, SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 64.0F, 0.8F + this.world.rand.nextFloat() * 0.3F, false);
      }

   }

   public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
      if (progress >= 0 && progress < 10) {
         DestroyBlockProgress destroyblockprogress = this.damagedBlocks.get(breakerId);
         if (destroyblockprogress == null || destroyblockprogress.getPosition().getX() != pos.getX() || destroyblockprogress.getPosition().getY() != pos.getY() || destroyblockprogress.getPosition().getZ() != pos.getZ()) {
            destroyblockprogress = new DestroyBlockProgress(breakerId, pos);
            this.damagedBlocks.put(breakerId, destroyblockprogress);
         }

         destroyblockprogress.setPartialBlockDamage(progress);
         destroyblockprogress.setCloudUpdateTick(this.ticks);
      } else {
         this.damagedBlocks.remove(breakerId);
      }

   }

   public boolean hasNoChunkUpdates() {
      return this.chunksToUpdate.isEmpty() && this.renderDispatcher.hasNoChunkUpdates();
   }

   public void setDisplayListEntitiesDirty() {
      this.displayListEntitiesDirty = true;
      this.cloudsNeedUpdate = true;
   }

   public void updateTileEntities(Collection<TileEntity> tileEntitiesToRemove, Collection<TileEntity> tileEntitiesToAdd) {
      synchronized(this.setTileEntities) {
         this.setTileEntities.removeAll(tileEntitiesToRemove);
         this.setTileEntities.addAll(tileEntitiesToAdd);
      }
   }

   @Override
   public net.minecraftforge.resource.IResourceType getResourceType() {
      return net.minecraftforge.resource.VanillaResourceType.MODELS;
   }

   @OnlyIn(Dist.CLIENT)
   class ContainerLocalRenderInformation {
      private final RenderChunk renderChunk;
      private final EnumFacing facing;
      private byte setFacing;
      private final int counter;

      private ContainerLocalRenderInformation(RenderChunk renderChunkIn, @Nullable EnumFacing facingIn, int counterIn) {
         this.renderChunk = renderChunkIn;
         this.facing = facingIn;
         this.counter = counterIn;
      }

      public void setDirection(byte dir, EnumFacing facingIn) {
         this.setFacing = (byte)(this.setFacing | dir | 1 << facingIn.ordinal());
      }

      public boolean hasDirection(EnumFacing facingIn) {
         return (this.setFacing & 1 << facingIn.ordinal()) > 0;
      }
   }
}
