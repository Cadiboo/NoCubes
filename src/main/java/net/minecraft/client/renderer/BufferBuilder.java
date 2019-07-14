package net.minecraft.client.renderer;

import com.google.common.primitives.Floats;
//import cup.1;
//import cup.a;
//import cuw.b;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.optifine.Config;
//import net.optifine.SmartAnimations;
import net.optifine.render.RenderEnv;
import net.optifine.shaders.SVertexBuilder;
//import net.optifine.util.TextureUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

//public class cup {
@SuppressWarnings("all")
public class BufferBuilder {
   public static final Logger a = LogManager.getLogger();
   public ByteBuffer b;
   public IntBuffer c;
   public ShortBuffer d;
   public FloatBuffer e;
   public int f;
//   public cuw g;
   public int h;
   public boolean i;
   public int j;
   public double k;
   public double l;
   public double m;
//   public cuv n;
   public boolean o;
//   public bgx blockLayer = null;
   public boolean[] drawnIcons = new boolean[256];
//   public dun[] quadSprites = null;
//   public dun[] quadSpritesPrev = null;
//   public dun quadSprite = null;
   public SVertexBuilder sVertexBuilder;
   public RenderEnv renderEnv = null;
   public BitSet animatedSprites = null;
   public BitSet animatedSpritesCached = new BitSet();

   public BufferBuilder(int bufferSizeIn) {
//   public cup(int bufferSizeIn) {
//      this.b = ctz.c(bufferSizeIn * 4);
      this.c = this.b.asIntBuffer();
      this.d = this.b.asShortBuffer();
      this.e = this.b.asFloatBuffer();
//      SVertexBuilder.initVertexBuilder(this);
   }

//   public void b(int increaseAmount) {
//      if (this.f * this.n.g() + increaseAmount > this.b.capacity()) {
//         int i = this.b.capacity();
//         int j = i + c(increaseAmount);
//         a.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", i, j);
//         int k = this.c.position();
//         ByteBuffer bytebuffer = ctz.c(j);
//         this.b.position(0);
//         bytebuffer.put(this.b);
//         bytebuffer.rewind();
//         this.b = bytebuffer;
//         this.e = this.b.asFloatBuffer();
//         this.c = this.b.asIntBuffer();
//         this.c.position(k);
//         this.d = this.b.asShortBuffer();
//         this.d.position(k << 1);
//         if (this.quadSprites != null) {
//            dun[] sprites = this.quadSprites;
//            int quadSize = this.getBufferQuadSize();
//            this.quadSprites = new dun[quadSize];
//            System.arraycopy(sprites, 0, this.quadSprites, 0, Math.min(sprites.length, this.quadSprites.length));
//            this.quadSpritesPrev = null;
//         }
//      }
//
//   }
//
//   public static int c(int xIn) {
//      int i = 2097152;
//      if (xIn == 0) {
//         return i;
//      } else {
//         if (xIn < 0) {
//            i *= -1;
//         }
//
//         int j = xIn % i;
//         return j == 0 ? xIn : xIn + i - j;
//      }
//   }
//
//   public void a(float cameraX, float cameraY, float cameraZ) {
//      int i = this.f / 4;
//      float[] afloat = new float[i];
//
//      for(int j = 0; j < i; ++j) {
//         afloat[j] = a(this.e, (float)((double)cameraX + this.k), (float)((double)cameraY + this.l), (float)((double)cameraZ + this.m), this.n.f(), j * this.n.g());
//      }
//
//      Integer[] ainteger = new Integer[i];
//
//      for(int k = 0; k < ainteger.length; ++k) {
//         ainteger[k] = k;
//      }
//
//      Arrays.sort(ainteger, (p_210255_1_, p_210255_2_) -> {
//         return Floats.compare(afloat[p_210255_2_.intValue()], afloat[p_210255_1_.intValue()]);
//      });
//      BitSet bitset = new BitSet();
//      int l = this.n.g();
//      int[] aint = new int[l];
//
//      int j1;
//      int ix;
//      int l1;
//      for(int i1 = bitset.nextClearBit(0); i1 < ainteger.length; i1 = bitset.nextClearBit(i1 + 1)) {
//         j1 = ainteger[i1].intValue();
//         if (j1 != i1) {
//            this.c.limit(j1 * l + l);
//            this.c.position(j1 * l);
//            this.c.get(aint);
//            ix = j1;
//
//            for(l1 = ainteger[j1].intValue(); ix != i1; l1 = ainteger[l1].intValue()) {
//               this.c.limit(l1 * l + l);
//               this.c.position(l1 * l);
//               IntBuffer intbuffer = this.c.slice();
//               this.c.limit(ix * l + l);
//               this.c.position(ix * l);
//               this.c.put(intbuffer);
//               bitset.set(ix);
//               ix = l1;
//            }
//
//            this.c.limit(i1 * l + l);
//            this.c.position(i1 * l);
//            this.c.put(aint);
//         }
//
//         bitset.set(i1);
//      }
//
//      this.c.limit(this.c.capacity());
//      this.c.position(this.j());
//      if (this.quadSprites != null) {
//         dun[] quadSpritesSorted = new dun[this.f / 4];
//         j1 = this.n.g() / 4 * 4;
//
//         for(ix = 0; ix < ainteger.length; ++ix) {
//            l1 = ainteger[ix].intValue();
//            quadSpritesSorted[ix] = this.quadSprites[l1];
//         }
//
//         System.arraycopy(quadSpritesSorted, 0, this.quadSprites, 0, quadSpritesSorted.length);
//      }
//
//   }
//
//   public a a() {
//      this.c.rewind();
//      int i = this.j();
//      this.c.limit(i);
//      int[] aint = new int[i];
//      this.c.get(aint);
//      this.c.limit(this.c.capacity());
//      this.c.position(i);
//      dun[] quadSpritesCopy = null;
//      if (this.quadSprites != null) {
//         int countQuads = this.f / 4;
//         quadSpritesCopy = new dun[countQuads];
//         System.arraycopy(this.quadSprites, 0, quadSpritesCopy, 0, countQuads);
//      }
//
//      return new a(this, aint, new cuv(this.n), quadSpritesCopy);
//   }
//
//   public int j() {
//      return this.f * this.n.f();
//   }
//
//   public static float a(FloatBuffer floatBufferIn, float x, float y, float z, int integerSize, int offset) {
//      float f = floatBufferIn.get(offset + integerSize * 0 + 0);
//      float f1 = floatBufferIn.get(offset + integerSize * 0 + 1);
//      float f2 = floatBufferIn.get(offset + integerSize * 0 + 2);
//      float f3 = floatBufferIn.get(offset + integerSize * 1 + 0);
//      float f4 = floatBufferIn.get(offset + integerSize * 1 + 1);
//      float f5 = floatBufferIn.get(offset + integerSize * 1 + 2);
//      float f6 = floatBufferIn.get(offset + integerSize * 2 + 0);
//      float f7 = floatBufferIn.get(offset + integerSize * 2 + 1);
//      float f8 = floatBufferIn.get(offset + integerSize * 2 + 2);
//      float f9 = floatBufferIn.get(offset + integerSize * 3 + 0);
//      float f10 = floatBufferIn.get(offset + integerSize * 3 + 1);
//      float f11 = floatBufferIn.get(offset + integerSize * 3 + 2);
//      float f12 = (f + f3 + f6 + f9) * 0.25F - x;
//      float f13 = (f1 + f4 + f7 + f10) * 0.25F - y;
//      float f14 = (f2 + f5 + f8 + f11) * 0.25F - z;
//      return f12 * f12 + f13 * f13 + f14 * f14;
//   }
//
//   public void a(a state) {
//      this.c.clear();
//      this.b(state.a().length * 4);
//      this.c.put(state.a());
//      this.f = state.b();
//      this.n = new cuv(state.c());
//      if (cup.a.access$000(state) != null) {
//         if (this.quadSprites == null) {
//            this.quadSprites = this.quadSpritesPrev;
//         }
//
//         if (this.quadSprites == null || this.quadSprites.length < this.getBufferQuadSize()) {
//            this.quadSprites = new dun[this.getBufferQuadSize()];
//         }
//
//         dun[] src = cup.a.access$000(state);
//         System.arraycopy(src, 0, this.quadSprites, 0, src.length);
//      } else {
//         if (this.quadSprites != null) {
//            this.quadSpritesPrev = this.quadSprites;
//         }
//
//         this.quadSprites = null;
//      }
//
//   }
//
//   public void b() {
//      this.f = 0;
//      this.g = null;
//      this.h = 0;
//      this.quadSprite = null;
//      if (SmartAnimations.isActive()) {
//         if (this.animatedSprites == null) {
//            this.animatedSprites = this.animatedSpritesCached;
//         }
//
//         this.animatedSprites.clear();
//      } else if (this.animatedSprites != null) {
//         this.animatedSprites = null;
//      }
//
//   }
//
//   public void a(int glMode, cuv format) {
//      if (this.o) {
//         throw new IllegalStateException("Already building!");
//      } else {
//         this.o = true;
//         this.b();
//         this.j = glMode;
//         this.n = format;
//         this.g = format.c(this.h);
//         this.i = false;
//         this.b.limit(this.b.capacity());
//         if (Config.isShaders()) {
//            SVertexBuilder.endSetVertexFormat(this);
//         }
//
//         if (Config.isMultiTexture()) {
//            if (this.blockLayer != null) {
//               if (this.quadSprites == null) {
//                  this.quadSprites = this.quadSpritesPrev;
//               }
//
//               if (this.quadSprites == null || this.quadSprites.length < this.getBufferQuadSize()) {
//                  this.quadSprites = new dun[this.getBufferQuadSize()];
//               }
//            }
//         } else {
//            if (this.quadSprites != null) {
//               this.quadSpritesPrev = this.quadSprites;
//            }
//
//            this.quadSprites = null;
//         }
//
//      }
//   }
//
//   public cup a(double u, double v) {
//      if (this.quadSprite != null && this.quadSprites != null) {
//         u = (double)this.quadSprite.toSingleU((float)u);
//         v = (double)this.quadSprite.toSingleV((float)v);
//         this.quadSprites[this.f / 4] = this.quadSprite;
//      }
//
//      int i = this.f * this.n.g() + this.n.d(this.h);
//      switch(1.$SwitchMap$net$minecraft$client$renderer$vertex$VertexFormatElement$Type[this.g.a().ordinal()]) {
//      case 1:
//         this.b.putFloat(i, (float)u);
//         this.b.putFloat(i + 4, (float)v);
//         break;
//      case 2:
//      case 3:
//         this.b.putInt(i, (int)u);
//         this.b.putInt(i + 4, (int)v);
//         break;
//      case 4:
//      case 5:
//         this.b.putShort(i, (short)((int)v));
//         this.b.putShort(i + 2, (short)((int)u));
//         break;
//      case 6:
//      case 7:
//         this.b.put(i, (byte)((int)v));
//         this.b.put(i + 1, (byte)((int)u));
//      }
//
//      this.k();
//      return this;
//   }
//
//   public cup a(int skyLight, int blockLight) {
//      int i = this.f * this.n.g() + this.n.d(this.h);
//      switch(1.$SwitchMap$net$minecraft$client$renderer$vertex$VertexFormatElement$Type[this.g.a().ordinal()]) {
//      case 1:
//         this.b.putFloat(i, (float)skyLight);
//         this.b.putFloat(i + 4, (float)blockLight);
//         break;
//      case 2:
//      case 3:
//         this.b.putInt(i, skyLight);
//         this.b.putInt(i + 4, blockLight);
//         break;
//      case 4:
//      case 5:
//         this.b.putShort(i, (short)blockLight);
//         this.b.putShort(i + 2, (short)skyLight);
//         break;
//      case 6:
//      case 7:
//         this.b.put(i, (byte)blockLight);
//         this.b.put(i + 1, (byte)skyLight);
//      }
//
//      this.k();
//      return this;
//   }
//
//   public void a(int vertex0, int vertex1, int vertex2, int vertex3) {
//      int i = (this.f - 4) * this.n.f() + this.n.b(1) / 4;
//      int j = this.n.g() >> 2;
//      this.c.put(i, vertex0);
//      this.c.put(i + j, vertex1);
//      this.c.put(i + j * 2, vertex2);
//      this.c.put(i + j * 3, vertex3);
//   }
//
//   public void a(double x, double y, double z) {
//      int i = this.n.f();
//      int j = (this.f - 4) * i;
//
//      for(int k = 0; k < 4; ++k) {
//         int l = j + k * i;
//         int i1 = l + 1;
//         int j1 = i1 + 1;
//         this.c.put(l, Float.floatToRawIntBits((float)(x + this.k) + Float.intBitsToFloat(this.c.get(l))));
//         this.c.put(i1, Float.floatToRawIntBits((float)(y + this.l) + Float.intBitsToFloat(this.c.get(i1))));
//         this.c.put(j1, Float.floatToRawIntBits((float)(z + this.m) + Float.intBitsToFloat(this.c.get(j1))));
//      }
//
//   }
//
//   public int d(int vertexIndex) {
//      return ((this.f - vertexIndex) * this.n.g() + this.n.e()) / 4;
//   }
//
//   public void a(float red, float green, float blue, int vertexIndex) {
//      int i = this.d(vertexIndex);
//      int j = -1;
//      if (!this.i) {
//         j = this.c.get(i);
//         int k;
//         int l;
//         int i1;
//         if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
//            k = (int)((float)(j & 255) * red);
//            l = (int)((float)(j >> 8 & 255) * green);
//            i1 = (int)((float)(j >> 16 & 255) * blue);
//            j &= -16777216;
//            j = j | i1 << 16 | l << 8 | k;
//         } else {
//            k = (int)((float)(j >> 24 & 255) * red);
//            l = (int)((float)(j >> 16 & 255) * green);
//            i1 = (int)((float)(j >> 8 & 255) * blue);
//            j &= 255;
//            j = j | k << 24 | l << 16 | i1 << 8;
//         }
//      }
//
//      this.c.put(i, j);
//   }
//
//   public void b(int argb, int vertexIndex) {
//      int i = this.d(vertexIndex);
//      int j = argb >> 16 & 255;
//      int k = argb >> 8 & 255;
//      int l = argb & 255;
//      this.c(i, j, k, l);
//   }
//
//   public void b(float red, float green, float blue, int vertexIndex) {
//      int i = this.d(vertexIndex);
//      int j = a((int)(red * 255.0F), 0, 255);
//      int k = a((int)(green * 255.0F), 0, 255);
//      int l = a((int)(blue * 255.0F), 0, 255);
//      this.c(i, j, k, l);
//   }
//
//   public static int a(int xIn, int xMin, int xMax) {
//      if (xIn < xMin) {
//         return xMin;
//      } else {
//         return xIn > xMax ? xMax : xIn;
//      }
//   }
//
//   public void c(int index, int red, int green, int blue) {
//      if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
//         this.c.put(index, -16777216 | blue << 16 | green << 8 | red);
//      } else {
//         this.c.put(index, red << 24 | green << 16 | blue << 8 | 255);
//      }
//
//   }
//
//   public void c() {
//      this.i = true;
//   }
//
//   public cup a(float red, float green, float blue, float alpha) {
//      return this.b((int)(red * 255.0F), (int)(green * 255.0F), (int)(blue * 255.0F), (int)(alpha * 255.0F));
//   }
//
//   public cup b(int red, int green, int blue, int alpha) {
//      if (this.i) {
//         return this;
//      } else {
//         int i = this.f * this.n.g() + this.n.d(this.h);
//         switch(1.$SwitchMap$net$minecraft$client$renderer$vertex$VertexFormatElement$Type[this.g.a().ordinal()]) {
//         case 1:
//            this.b.putFloat(i, (float)red / 255.0F);
//            this.b.putFloat(i + 4, (float)green / 255.0F);
//            this.b.putFloat(i + 8, (float)blue / 255.0F);
//            this.b.putFloat(i + 12, (float)alpha / 255.0F);
//            break;
//         case 2:
//         case 3:
//            this.b.putFloat(i, (float)red);
//            this.b.putFloat(i + 4, (float)green);
//            this.b.putFloat(i + 8, (float)blue);
//            this.b.putFloat(i + 12, (float)alpha);
//            break;
//         case 4:
//         case 5:
//            this.b.putShort(i, (short)red);
//            this.b.putShort(i + 2, (short)green);
//            this.b.putShort(i + 4, (short)blue);
//            this.b.putShort(i + 6, (short)alpha);
//            break;
//         case 6:
//         case 7:
//            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
//               this.b.put(i, (byte)red);
//               this.b.put(i + 1, (byte)green);
//               this.b.put(i + 2, (byte)blue);
//               this.b.put(i + 3, (byte)alpha);
//            } else {
//               this.b.put(i, (byte)alpha);
//               this.b.put(i + 1, (byte)blue);
//               this.b.put(i + 2, (byte)green);
//               this.b.put(i + 3, (byte)red);
//            }
//         }
//
//         this.k();
//         return this;
//      }
//   }
//
//   public void a(int[] vertexData) {
//      if (Config.isShaders()) {
//         SVertexBuilder.beginAddVertexData(this, vertexData);
//      }
//
//      this.b(vertexData.length * 4 + this.n.g());
//      this.c.position(this.j());
//      this.c.put(vertexData);
//      this.f += vertexData.length / this.n.f();
//      if (Config.isShaders()) {
//         SVertexBuilder.endAddVertexData(this);
//      }
//
//   }
//
//   public void d() {
//      ++this.f;
//      this.b(this.n.g());
//      this.h = 0;
//      this.g = this.n.c(this.h);
//      if (Config.isShaders()) {
//         SVertexBuilder.endAddVertex(this);
//      }
//
//   }
//
//   public cup b(double x, double y, double z) {
//      if (Config.isShaders()) {
//         SVertexBuilder.beginAddVertex(this);
//      }
//
//      int i = this.f * this.n.g() + this.n.d(this.h);
//      switch(1.$SwitchMap$net$minecraft$client$renderer$vertex$VertexFormatElement$Type[this.g.a().ordinal()]) {
//      case 1:
//         this.b.putFloat(i, (float)(x + this.k));
//         this.b.putFloat(i + 4, (float)(y + this.l));
//         this.b.putFloat(i + 8, (float)(z + this.m));
//         break;
//      case 2:
//      case 3:
//         this.b.putInt(i, Float.floatToRawIntBits((float)(x + this.k)));
//         this.b.putInt(i + 4, Float.floatToRawIntBits((float)(y + this.l)));
//         this.b.putInt(i + 8, Float.floatToRawIntBits((float)(z + this.m)));
//         break;
//      case 4:
//      case 5:
//         this.b.putShort(i, (short)((int)(x + this.k)));
//         this.b.putShort(i + 2, (short)((int)(y + this.l)));
//         this.b.putShort(i + 4, (short)((int)(z + this.m)));
//         break;
//      case 6:
//      case 7:
//         this.b.put(i, (byte)((int)(x + this.k)));
//         this.b.put(i + 1, (byte)((int)(y + this.l)));
//         this.b.put(i + 2, (byte)((int)(z + this.m)));
//      }
//
//      this.k();
//      return this;
//   }
//
//   public void b(float x, float y, float z) {
//      int i = (byte)((int)(x * 127.0F)) & 255;
//      int j = (byte)((int)(y * 127.0F)) & 255;
//      int k = (byte)((int)(z * 127.0F)) & 255;
//      int l = i | j << 8 | k << 16;
//      int i1 = this.n.g() >> 2;
//      int j1 = (this.f - 4) * i1 + this.n.c() / 4;
//      this.c.put(j1, l);
//      this.c.put(j1 + i1, l);
//      this.c.put(j1 + i1 * 2, l);
//      this.c.put(j1 + i1 * 3, l);
//   }
//
//   public void k() {
//      ++this.h;
//      this.h %= this.n.i();
//      this.g = this.n.c(this.h);
//      if (this.g.b() == cuw.b.g) {
//         this.k();
//      }
//
//   }
//
//   public cup c(float x, float y, float z) {
//      int i = this.f * this.n.g() + this.n.d(this.h);
//      switch(1.$SwitchMap$net$minecraft$client$renderer$vertex$VertexFormatElement$Type[this.g.a().ordinal()]) {
//      case 1:
//         this.b.putFloat(i, x);
//         this.b.putFloat(i + 4, y);
//         this.b.putFloat(i + 8, z);
//         break;
//      case 2:
//      case 3:
//         this.b.putInt(i, (int)x);
//         this.b.putInt(i + 4, (int)y);
//         this.b.putInt(i + 8, (int)z);
//         break;
//      case 4:
//      case 5:
//         this.b.putShort(i, (short)((int)(x * 32767.0F) & '\uffff'));
//         this.b.putShort(i + 2, (short)((int)(y * 32767.0F) & '\uffff'));
//         this.b.putShort(i + 4, (short)((int)(z * 32767.0F) & '\uffff'));
//         break;
//      case 6:
//      case 7:
//         this.b.put(i, (byte)((int)(x * 127.0F) & 255));
//         this.b.put(i + 1, (byte)((int)(y * 127.0F) & 255));
//         this.b.put(i + 2, (byte)((int)(z * 127.0F) & 255));
//      }
//
//      this.k();
//      return this;
//   }
//
//   public void c(double x, double y, double z) {
//      this.k = x;
//      this.l = y;
//      this.m = z;
//   }
//
//   public void e() {
//      if (!this.o) {
//         throw new IllegalStateException("Not building!");
//      } else {
//         this.o = false;
//         this.b.position(0);
//         this.b.limit(this.j() * 4);
//      }
//   }
//
//   public ByteBuffer f() {
//      return this.b;
//   }
//
//   public cuv g() {
//      return this.n;
//   }
//
//   public int h() {
//      return this.f;
//   }
//
//   public int i() {
//      return this.j;
//   }
//
//   public void a(int argb) {
//      for(int i = 0; i < 4; ++i) {
//         this.b(argb, i + 1);
//      }
//
//   }
//
//   public void d(float red, float green, float blue) {
//      for(int i = 0; i < 4; ++i) {
//         this.b(red, green, blue, i + 1);
//      }
//
//   }
//
//   public void putSprite(dun sprite) {
//      if (this.animatedSprites != null && sprite != null && sprite.getAnimationIndex() >= 0) {
//         this.animatedSprites.set(sprite.getAnimationIndex());
//      }
//
//      if (this.quadSprites != null) {
//         int countQuads = this.f / 4;
//         this.quadSprites[countQuads - 1] = sprite;
//      }
//
//   }
//
//   public void setSprite(dun sprite) {
//      if (this.animatedSprites != null && sprite != null && sprite.getAnimationIndex() >= 0) {
//         this.animatedSprites.set(sprite.getAnimationIndex());
//      }
//
//      if (this.quadSprites != null) {
//         this.quadSprite = sprite;
//      }
//
//   }
//
//   public boolean isMultiTexture() {
//      return this.quadSprites != null;
//   }
//
//   public void drawMultiTexture() {
//      if (this.quadSprites != null) {
//         int maxTextureIndex = Config.getMinecraft().L().getCountRegisteredSprites();
//         if (this.drawnIcons.length <= maxTextureIndex) {
//            this.drawnIcons = new boolean[maxTextureIndex + 1];
//         }
//
//         Arrays.fill(this.drawnIcons, false);
//         int texSwitch = 0;
//         int grassOverlayIndex = -1;
//         int countQuads = this.f / 4;
//
//         for(int i = 0; i < countQuads; ++i) {
//            dun icon = this.quadSprites[i];
//            if (icon != null) {
//               int iconIndex = icon.getIndexInMap();
//               if (!this.drawnIcons[iconIndex]) {
//                  if (icon == TextureUtils.iconGrassSideOverlay) {
//                     if (grassOverlayIndex < 0) {
//                        grassOverlayIndex = i;
//                     }
//                  } else {
//                     i = this.drawForIcon(icon, i) - 1;
//                     ++texSwitch;
//                     if (this.blockLayer != bgx.d) {
//                        this.drawnIcons[iconIndex] = true;
//                     }
//                  }
//               }
//            }
//         }
//
//         if (grassOverlayIndex >= 0) {
//            this.drawForIcon(TextureUtils.iconGrassSideOverlay, grassOverlayIndex);
//            ++texSwitch;
//         }
//
//         if (texSwitch > 0) {
//            ;
//         }
//
//      }
//   }
//
//   public int drawForIcon(dun sprite, int startQuadPos) {
//      GL11.glBindTexture(3553, sprite.glSpriteTextureId);
//      int firstRegionEnd = -1;
//      int lastPos = -1;
//      int countQuads = this.f / 4;
//
//      for(int i = startQuadPos; i < countQuads; ++i) {
//         dun ts = this.quadSprites[i];
//         if (ts == sprite) {
//            if (lastPos < 0) {
//               lastPos = i;
//            }
//         } else if (lastPos >= 0) {
//            this.draw(lastPos, i);
//            if (this.blockLayer == bgx.d) {
//               return i;
//            }
//
//            lastPos = -1;
//            if (firstRegionEnd < 0) {
//               firstRegionEnd = i;
//            }
//         }
//      }
//
//      if (lastPos >= 0) {
//         this.draw(lastPos, countQuads);
//      }
//
//      if (firstRegionEnd < 0) {
//         firstRegionEnd = countQuads;
//      }
//
//      return firstRegionEnd;
//   }
//
//   public void draw(int startQuadVertex, int endQuadVertex) {
//      int vxQuadCount = endQuadVertex - startQuadVertex;
//      if (vxQuadCount > 0) {
//         int startVertex = startQuadVertex * 4;
//         int vxCount = vxQuadCount * 4;
//         GL11.glDrawArrays(this.j, startVertex, vxCount);
//      }
//   }
//
//   public void setBlockLayer(bgx blockLayer) {
//      this.blockLayer = blockLayer;
//      if (blockLayer == null) {
//         if (this.quadSprites != null) {
//            this.quadSpritesPrev = this.quadSprites;
//         }
//
//         this.quadSprites = null;
//         this.quadSprite = null;
//      }
//
//   }
//
//   public int getBufferQuadSize() {
//      int quadSize = this.c.capacity() * 4 / (this.n.f() * 4);
//      return quadSize;
//   }

   public RenderEnv getRenderEnv(BlockState blockStateIn, BlockPos blockPosIn) {
//   public RenderEnv getRenderEnv(bvo blockStateIn, ev blockPosIn) {
      if (this.renderEnv == null) {
         this.renderEnv = new RenderEnv(blockStateIn, blockPosIn);
         return this.renderEnv;
      } else {
         this.renderEnv.reset(blockStateIn, blockPosIn);
         return this.renderEnv;
      }
   }

//   public boolean isDrawing() {
//      return this.o;
//   }
//
//   public double getXOffset() {
//      return this.k;
//   }
//
//   public double getYOffset() {
//      return this.l;
//   }
//
//   public double getZOffset() {
//      return this.m;
//   }
//
//   public bgx getBlockLayer() {
//      return this.blockLayer;
//   }
//
//   public void putColorMultiplierRgba(float red, float green, float blue, float alpha, int vertexIndex) {
//      int index = this.d(vertexIndex);
//      int col = -1;
//      if (!this.i) {
//         col = this.c.get(index);
//         int r;
//         int g;
//         int b;
//         int a;
//         if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
//            r = (int)((float)(col & 255) * red);
//            g = (int)((float)(col >> 8 & 255) * green);
//            b = (int)((float)(col >> 16 & 255) * blue);
//            a = (int)((float)(col >> 24 & 255) * alpha);
//            col = a << 24 | b << 16 | g << 8 | r;
//         } else {
//            r = (int)((float)(col >> 24 & 255) * red);
//            g = (int)((float)(col >> 16 & 255) * green);
//            b = (int)((float)(col >> 8 & 255) * blue);
//            a = (int)((float)(col & 255) * alpha);
//            col = r << 24 | g << 16 | b << 8 | a;
//         }
//      }
//
//      this.c.put(index, col);
//   }
//
//   public void putColorRGBA(int index, int red, int green, int blue, int alpha) {
//      if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
//         this.c.put(index, alpha << 24 | blue << 16 | green << 8 | red);
//      } else {
//         this.c.put(index, red << 24 | green << 16 | blue << 8 | alpha);
//      }
//
//   }
//
//   public boolean isColorDisabled() {
//      return this.i;
//   }
//
//   public void putBulkData(ByteBuffer buffer) {
//      if (Config.isShaders()) {
//         SVertexBuilder.beginAddVertexData(this, buffer);
//      }
//
//      this.b(buffer.limit() + this.n.g());
//      this.b.position(this.f * this.n.g());
//      this.b.put(buffer);
//      this.f += buffer.limit() / this.n.g();
//      if (Config.isShaders()) {
//         SVertexBuilder.endAddVertexData(this);
//      }
//
//   }

   // Normal bufferbuilder code so I can compile shit

   private static final Logger LOGGER = LogManager.getLogger();
   private ByteBuffer byteBuffer;
   private IntBuffer rawIntBuffer;
   private ShortBuffer rawShortBuffer;
   private FloatBuffer rawFloatBuffer;
   private int vertexCount;
   private VertexFormatElement vertexFormatElement;
   private int vertexFormatIndex;
   private boolean noColor;
   private int drawMode;
   private double xOffset;
   private double yOffset;
   private double zOffset;
   private VertexFormat vertexFormat;
   private boolean isDrawing;

//   public BufferBuilder(int bufferSizeIn) {
//      this.byteBuffer = GLAllocation.createDirectByteBuffer(bufferSizeIn * 4);
//      this.rawIntBuffer = this.byteBuffer.asIntBuffer();
//      this.rawShortBuffer = this.byteBuffer.asShortBuffer();
//      this.rawFloatBuffer = this.byteBuffer.asFloatBuffer();
//   }

   private void growBuffer(int increaseAmount) {
      if (this.vertexCount * this.vertexFormat.getSize() + increaseAmount > this.byteBuffer.capacity()) {
         int i = this.byteBuffer.capacity();
         int j = i + func_216566_c(increaseAmount);
         LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", i, j);
         int k = this.rawIntBuffer.position();
         ByteBuffer bytebuffer = GLAllocation.createDirectByteBuffer(j);
         this.byteBuffer.position(0);
         bytebuffer.put(this.byteBuffer);
         bytebuffer.rewind();
         this.byteBuffer = bytebuffer;
         this.rawFloatBuffer = this.byteBuffer.asFloatBuffer().asReadOnlyBuffer();
         this.rawIntBuffer = this.byteBuffer.asIntBuffer();
         this.rawIntBuffer.position(k);
         this.rawShortBuffer = this.byteBuffer.asShortBuffer();
         this.rawShortBuffer.position(k << 1);
      }
   }

   private static int func_216566_c(int p_216566_0_) {
      int i = 2097152;
      if (p_216566_0_ == 0) {
         return i;
      } else {
         if (p_216566_0_ < 0) {
            i *= -1;
         }

         int j = p_216566_0_ % i;
         return j == 0 ? p_216566_0_ : p_216566_0_ + i - j;
      }
   }

   public void sortVertexData(float cameraX, float cameraY, float cameraZ) {
      int i = this.vertexCount / 4;
      float[] afloat = new float[i];

      for(int j = 0; j < i; ++j) {
         afloat[j] = getDistanceSq(this.rawFloatBuffer, (float)((double)cameraX + this.xOffset), (float)((double)cameraY + this.yOffset), (float)((double)cameraZ + this.zOffset), this.vertexFormat.getIntegerSize(), j * this.vertexFormat.getSize());
      }

      Integer[] ainteger = new Integer[i];

      for(int k = 0; k < ainteger.length; ++k) {
         ainteger[k] = k;
      }

      Arrays.sort(ainteger, (p_210255_1_, p_210255_2_) -> {
         return Floats.compare(afloat[p_210255_2_], afloat[p_210255_1_]);
      });
      BitSet bitset = new BitSet();
      int l = this.vertexFormat.getSize();
      int[] aint = new int[l];

      for(int i1 = bitset.nextClearBit(0); i1 < ainteger.length; i1 = bitset.nextClearBit(i1 + 1)) {
         int j1 = ainteger[i1];
         if (j1 != i1) {
            this.rawIntBuffer.limit(j1 * l + l);
            this.rawIntBuffer.position(j1 * l);
            this.rawIntBuffer.get(aint);
            int k1 = j1;

            for(int l1 = ainteger[j1]; k1 != i1; l1 = ainteger[l1]) {
               this.rawIntBuffer.limit(l1 * l + l);
               this.rawIntBuffer.position(l1 * l);
               IntBuffer intbuffer = this.rawIntBuffer.slice();
               this.rawIntBuffer.limit(k1 * l + l);
               this.rawIntBuffer.position(k1 * l);
               this.rawIntBuffer.put(intbuffer);
               bitset.set(k1);
               k1 = l1;
            }

            this.rawIntBuffer.limit(i1 * l + l);
            this.rawIntBuffer.position(i1 * l);
            this.rawIntBuffer.put(aint);
         }

         bitset.set(i1);
      }
      this.rawIntBuffer.limit(this.rawIntBuffer.capacity());
      this.rawIntBuffer.position(this.getBufferSize());
   }

   public BufferBuilder.State getVertexState() {
      this.rawIntBuffer.rewind();
      int i = this.getBufferSize();
      this.rawIntBuffer.limit(i);
      int[] aint = new int[i];
      this.rawIntBuffer.get(aint);
      this.rawIntBuffer.limit(this.rawIntBuffer.capacity());
      this.rawIntBuffer.position(i);
      return new BufferBuilder.State(aint, new VertexFormat(this.vertexFormat));
   }

   private int getBufferSize() {
      return this.vertexCount * this.vertexFormat.getIntegerSize();
   }

   private static float getDistanceSq(FloatBuffer floatBufferIn, float x, float y, float z, int integerSize, int offset) {
      float f = floatBufferIn.get(offset + integerSize * 0 + 0);
      float f1 = floatBufferIn.get(offset + integerSize * 0 + 1);
      float f2 = floatBufferIn.get(offset + integerSize * 0 + 2);
      float f3 = floatBufferIn.get(offset + integerSize * 1 + 0);
      float f4 = floatBufferIn.get(offset + integerSize * 1 + 1);
      float f5 = floatBufferIn.get(offset + integerSize * 1 + 2);
      float f6 = floatBufferIn.get(offset + integerSize * 2 + 0);
      float f7 = floatBufferIn.get(offset + integerSize * 2 + 1);
      float f8 = floatBufferIn.get(offset + integerSize * 2 + 2);
      float f9 = floatBufferIn.get(offset + integerSize * 3 + 0);
      float f10 = floatBufferIn.get(offset + integerSize * 3 + 1);
      float f11 = floatBufferIn.get(offset + integerSize * 3 + 2);
      float f12 = (f + f3 + f6 + f9) * 0.25F - x;
      float f13 = (f1 + f4 + f7 + f10) * 0.25F - y;
      float f14 = (f2 + f5 + f8 + f11) * 0.25F - z;
      return f12 * f12 + f13 * f13 + f14 * f14;
   }

   public void setVertexState(BufferBuilder.State state) {
      this.rawIntBuffer.clear();
      this.growBuffer(state.getRawBuffer().length * 4);
      this.rawIntBuffer.put(state.getRawBuffer());
      this.vertexCount = state.getVertexCount();
      this.vertexFormat = new VertexFormat(state.getVertexFormat());
   }

   public void reset() {
      this.vertexCount = 0;
      this.vertexFormatElement = null;
      this.vertexFormatIndex = 0;
   }

   public void begin(int glMode, VertexFormat format) {
      if (this.isDrawing) {
         throw new IllegalStateException("Already building!");
      } else {
         this.isDrawing = true;
         this.reset();
         this.drawMode = glMode;
         this.vertexFormat = format;
         this.vertexFormatElement = format.getElement(this.vertexFormatIndex);
         this.noColor = false;
         this.byteBuffer.limit(this.byteBuffer.capacity());
      }
   }

   public BufferBuilder tex(double u, double v) {
      int i = this.vertexCount * this.vertexFormat.getSize() + this.vertexFormat.getOffset(this.vertexFormatIndex);
      switch(this.vertexFormatElement.getType()) {
         case FLOAT:
            this.byteBuffer.putFloat(i, (float)u);
            this.byteBuffer.putFloat(i + 4, (float)v);
            break;
         case UINT:
         case INT:
            this.byteBuffer.putInt(i, (int)u);
            this.byteBuffer.putInt(i + 4, (int)v);
            break;
         case USHORT:
         case SHORT:
            this.byteBuffer.putShort(i, (short)((int)v));
            this.byteBuffer.putShort(i + 2, (short)((int)u));
            break;
         case UBYTE:
         case BYTE:
            this.byteBuffer.put(i, (byte)((int)v));
            this.byteBuffer.put(i + 1, (byte)((int)u));
      }

      this.nextVertexFormatIndex();
      return this;
   }

   public BufferBuilder lightmap(int skyLight, int blockLight) {
      int i = this.vertexCount * this.vertexFormat.getSize() + this.vertexFormat.getOffset(this.vertexFormatIndex);
      switch(this.vertexFormatElement.getType()) {
         case FLOAT:
            this.byteBuffer.putFloat(i, (float)skyLight);
            this.byteBuffer.putFloat(i + 4, (float)blockLight);
            break;
         case UINT:
         case INT:
            this.byteBuffer.putInt(i, skyLight);
            this.byteBuffer.putInt(i + 4, blockLight);
            break;
         case USHORT:
         case SHORT:
            this.byteBuffer.putShort(i, (short)blockLight);
            this.byteBuffer.putShort(i + 2, (short)skyLight);
            break;
         case UBYTE:
         case BYTE:
            this.byteBuffer.put(i, (byte)blockLight);
            this.byteBuffer.put(i + 1, (byte)skyLight);
      }

      this.nextVertexFormatIndex();
      return this;
   }

   /**
    * Set the brightness for the previously stored quad (4 vertices)
    */
   public void putBrightness4(int vertex0, int vertex1, int vertex2, int vertex3) {
      int i = (this.vertexCount - 4) * this.vertexFormat.getIntegerSize() + this.vertexFormat.getUvOffsetById(1) / 4;
      int j = this.vertexFormat.getSize() >> 2;
      this.rawIntBuffer.put(i, vertex0);
      this.rawIntBuffer.put(i + j, vertex1);
      this.rawIntBuffer.put(i + j * 2, vertex2);
      this.rawIntBuffer.put(i + j * 3, vertex3);
   }

   public void putPosition(double x, double y, double z) {
      int i = this.vertexFormat.getIntegerSize();
      int j = (this.vertexCount - 4) * i;

      for(int k = 0; k < 4; ++k) {
         int l = j + k * i;
         int i1 = l + 1;
         int j1 = i1 + 1;
         this.rawIntBuffer.put(l, Float.floatToRawIntBits((float)(x + this.xOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(l))));
         this.rawIntBuffer.put(i1, Float.floatToRawIntBits((float)(y + this.yOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(i1))));
         this.rawIntBuffer.put(j1, Float.floatToRawIntBits((float)(z + this.zOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(j1))));
      }

   }

   /**
    * Gets the position into the vertex data buffer at which the given vertex's color data can be found, in {@code
    * int}s.
    */
   public int getColorIndex(int vertexIndex) {
      return ((this.vertexCount - vertexIndex) * this.vertexFormat.getSize() + this.vertexFormat.getColorOffset()) / 4;
   }

   /**
    * Modify the color data of the given vertex with the given multipliers.
    */
   public void putColorMultiplier(float red, float green, float blue, int vertexIndex) {
      int i = this.getColorIndex(vertexIndex);
      int j = -1;
      if (!this.noColor) {
         j = this.rawIntBuffer.get(i);
         if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            int k = (int)((float)(j & 255) * red);
            int l = (int)((float)(j >> 8 & 255) * green);
            int i1 = (int)((float)(j >> 16 & 255) * blue);
            j = j & -16777216;
            j = j | i1 << 16 | l << 8 | k;
         } else {
            int j1 = (int)((float)(j >> 24 & 255) * red);
            int k1 = (int)((float)(j >> 16 & 255) * green);
            int l1 = (int)((float)(j >> 8 & 255) * blue);
            j = j & 255;
            j = j | j1 << 24 | k1 << 16 | l1 << 8;
         }
      }

      this.rawIntBuffer.put(i, j);
   }

   private void putColor(int argb, int vertexIndex) {
      int i = this.getColorIndex(vertexIndex);
      int j = argb >> 16 & 255;
      int k = argb >> 8 & 255;
      int l = argb & 255;
      this.putColorRGBA(i, j, k, l);
   }

   public void putColorRGB_F(float red, float green, float blue, int vertexIndex) {
      int i = this.getColorIndex(vertexIndex);
      int j = func_216567_a((int)(red * 255.0F), 0, 255);
      int k = func_216567_a((int)(green * 255.0F), 0, 255);
      int l = func_216567_a((int)(blue * 255.0F), 0, 255);
      this.putColorRGBA(i, j, k, l);
   }

   private static int func_216567_a(int p_216567_0_, int p_216567_1_, int p_216567_2_) {
      if (p_216567_0_ < p_216567_1_) {
         return p_216567_1_;
      } else {
         return p_216567_0_ > p_216567_2_ ? p_216567_2_ : p_216567_0_;
      }
   }

   /**
    * Write the given color data of 4 bytes at the given index into the vertex data buffer, accounting for system
    * endianness.
    */
   public void putColorRGBA(int index, int red, int green, int blue) {
      if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
         this.rawIntBuffer.put(index, -16777216 | blue << 16 | green << 8 | red);
      } else {
         this.rawIntBuffer.put(index, red << 24 | green << 16 | blue << 8 | 255);
      }

   }

   /**
    * Disables color processing.
    */
   public void noColor() {
      this.noColor = true;
   }

   public BufferBuilder color(float red, float green, float blue, float alpha) {
      return this.color((int)(red * 255.0F), (int)(green * 255.0F), (int)(blue * 255.0F), (int)(alpha * 255.0F));
   }

   public BufferBuilder color(int red, int green, int blue, int alpha) {
      if (this.noColor) {
         return this;
      } else {
         int i = this.vertexCount * this.vertexFormat.getSize() + this.vertexFormat.getOffset(this.vertexFormatIndex);
         switch(this.vertexFormatElement.getType()) {
            case FLOAT:
               this.byteBuffer.putFloat(i, (float)red / 255.0F);
               this.byteBuffer.putFloat(i + 4, (float)green / 255.0F);
               this.byteBuffer.putFloat(i + 8, (float)blue / 255.0F);
               this.byteBuffer.putFloat(i + 12, (float)alpha / 255.0F);
               break;
            case UINT:
            case INT:
               this.byteBuffer.putFloat(i, (float)red);
               this.byteBuffer.putFloat(i + 4, (float)green);
               this.byteBuffer.putFloat(i + 8, (float)blue);
               this.byteBuffer.putFloat(i + 12, (float)alpha);
               break;
            case USHORT:
            case SHORT:
               this.byteBuffer.putShort(i, (short)red);
               this.byteBuffer.putShort(i + 2, (short)green);
               this.byteBuffer.putShort(i + 4, (short)blue);
               this.byteBuffer.putShort(i + 6, (short)alpha);
               break;
            case UBYTE:
            case BYTE:
               if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                  this.byteBuffer.put(i, (byte)red);
                  this.byteBuffer.put(i + 1, (byte)green);
                  this.byteBuffer.put(i + 2, (byte)blue);
                  this.byteBuffer.put(i + 3, (byte)alpha);
               } else {
                  this.byteBuffer.put(i, (byte)alpha);
                  this.byteBuffer.put(i + 1, (byte)blue);
                  this.byteBuffer.put(i + 2, (byte)green);
                  this.byteBuffer.put(i + 3, (byte)red);
               }
         }

         this.nextVertexFormatIndex();
         return this;
      }
   }

   public void addVertexData(int[] vertexData) {
      this.growBuffer(vertexData.length * 4 + this.vertexFormat.getSize());
      this.rawIntBuffer.position(this.getBufferSize());
      this.rawIntBuffer.put(vertexData);
      this.vertexCount += vertexData.length / this.vertexFormat.getIntegerSize();
   }

   public void endVertex() {
      ++this.vertexCount;
      this.growBuffer(this.vertexFormat.getSize());
   }

   public BufferBuilder pos(double x, double y, double z) {
      int i = this.vertexCount * this.vertexFormat.getSize() + this.vertexFormat.getOffset(this.vertexFormatIndex);
      switch(this.vertexFormatElement.getType()) {
         case FLOAT:
            this.byteBuffer.putFloat(i, (float)(x + this.xOffset));
            this.byteBuffer.putFloat(i + 4, (float)(y + this.yOffset));
            this.byteBuffer.putFloat(i + 8, (float)(z + this.zOffset));
            break;
         case UINT:
         case INT:
            this.byteBuffer.putInt(i, Float.floatToRawIntBits((float)(x + this.xOffset)));
            this.byteBuffer.putInt(i + 4, Float.floatToRawIntBits((float)(y + this.yOffset)));
            this.byteBuffer.putInt(i + 8, Float.floatToRawIntBits((float)(z + this.zOffset)));
            break;
         case USHORT:
         case SHORT:
            this.byteBuffer.putShort(i, (short)((int)(x + this.xOffset)));
            this.byteBuffer.putShort(i + 2, (short)((int)(y + this.yOffset)));
            this.byteBuffer.putShort(i + 4, (short)((int)(z + this.zOffset)));
            break;
         case UBYTE:
         case BYTE:
            this.byteBuffer.put(i, (byte)((int)(x + this.xOffset)));
            this.byteBuffer.put(i + 1, (byte)((int)(y + this.yOffset)));
            this.byteBuffer.put(i + 2, (byte)((int)(z + this.zOffset)));
      }

      this.nextVertexFormatIndex();
      return this;
   }

   public void putNormal(float x, float y, float z) {
      int i = (byte)((int)(x * 127.0F)) & 255;
      int j = (byte)((int)(y * 127.0F)) & 255;
      int k = (byte)((int)(z * 127.0F)) & 255;
      int l = i | j << 8 | k << 16;
      int i1 = this.vertexFormat.getSize() >> 2;
      int j1 = (this.vertexCount - 4) * i1 + this.vertexFormat.getNormalOffset() / 4;
      this.rawIntBuffer.put(j1, l);
      this.rawIntBuffer.put(j1 + i1, l);
      this.rawIntBuffer.put(j1 + i1 * 2, l);
      this.rawIntBuffer.put(j1 + i1 * 3, l);
   }

   private void nextVertexFormatIndex() {
      ++this.vertexFormatIndex;
      this.vertexFormatIndex %= this.vertexFormat.getElementCount();
      this.vertexFormatElement = this.vertexFormat.getElement(this.vertexFormatIndex);
      if (this.vertexFormatElement.getUsage() == VertexFormatElement.Usage.PADDING) {
         this.nextVertexFormatIndex();
      }

   }

   public BufferBuilder normal(float x, float y, float z) {
      int i = this.vertexCount * this.vertexFormat.getSize() + this.vertexFormat.getOffset(this.vertexFormatIndex);
      switch(this.vertexFormatElement.getType()) {
         case FLOAT:
            this.byteBuffer.putFloat(i, x);
            this.byteBuffer.putFloat(i + 4, y);
            this.byteBuffer.putFloat(i + 8, z);
            break;
         case UINT:
         case INT:
            this.byteBuffer.putInt(i, (int)x);
            this.byteBuffer.putInt(i + 4, (int)y);
            this.byteBuffer.putInt(i + 8, (int)z);
            break;
         case USHORT:
         case SHORT:
            this.byteBuffer.putShort(i, (short)((int)(x * Short.MAX_VALUE) & 0xFFFF));
            this.byteBuffer.putShort(i + 2, (short)((int)(y * Short.MAX_VALUE) & 0xFFFF));
            this.byteBuffer.putShort(i + 4, (short)((int)(z * Short.MAX_VALUE) & 0xFFFF));
            break;
         case UBYTE:
         case BYTE:
            this.byteBuffer.put(i, (byte)((int)(x * Byte.MAX_VALUE) & 0xFF));
            this.byteBuffer.put(i + 1, (byte)((int)(y * Byte.MAX_VALUE) & 0xFF));
            this.byteBuffer.put(i + 2, (byte)((int)(z * Byte.MAX_VALUE) & 0xFF));
      }

      this.nextVertexFormatIndex();
      return this;
   }

   public void setTranslation(double x, double y, double z) {
      this.xOffset = x;
      this.yOffset = y;
      this.zOffset = z;
   }

   public void finishDrawing() {
      if (!this.isDrawing) {
         throw new IllegalStateException("Not building!");
      } else {
         this.isDrawing = false;
         this.byteBuffer.position(0);
         this.byteBuffer.limit(this.getBufferSize() * 4);
      }
   }

   public ByteBuffer getByteBuffer() {
      return this.byteBuffer;
   }

   public VertexFormat getVertexFormat() {
      return this.vertexFormat;
   }

   public int getVertexCount() {
      return this.vertexCount;
   }

   public int getDrawMode() {
      return this.drawMode;
   }

   public void putColor4(int argb) {
      for(int i = 0; i < 4; ++i) {
         this.putColor(argb, i + 1);
      }

   }

   public void putColorRGB_F4(float red, float green, float blue) {
      for(int i = 0; i < 4; ++i) {
         this.putColorRGB_F(red, green, blue, i + 1);
      }

   }

   @OnlyIn(Dist.CLIENT)
   public class State {
      private final int[] stateRawBuffer;
      private final VertexFormat stateVertexFormat;

      public State(int[] buffer, VertexFormat format) {
         this.stateRawBuffer = buffer;
         this.stateVertexFormat = format;
      }

      public int[] getRawBuffer() {
         return this.stateRawBuffer;
      }

      public int getVertexCount() {
         return this.stateRawBuffer.length / this.stateVertexFormat.getIntegerSize();
      }

      public VertexFormat getVertexFormat() {
         return this.stateVertexFormat;
      }
   }

   //For some unknown reason Mojang changed the vanilla function to hardcode alpha as 255.... So lets re-add the parameter -.-
   public void putColorRGBA(int index, int red, int green, int blue, int alpha) {
      if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
         this.rawIntBuffer.put(index, alpha << 24 | blue << 16 | green << 8 | red);
      else
         this.rawIntBuffer.put(index, red << 24 | green << 16 | blue << 8 | alpha);
   }

   public boolean isColorDisabled() {
      return noColor;
   }

   public void putBulkData(ByteBuffer buffer) {
      growBuffer(buffer.limit() + this.vertexFormat.getSize());
      this.byteBuffer.position(this.vertexCount * this.vertexFormat.getSize());
      this.byteBuffer.put(buffer);
      this.vertexCount += buffer.limit() / this.vertexFormat.getSize();
   }

}
