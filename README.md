# [NoCubes Mod](https://Cadiboo.github.io/projects/nocubes/)
![Java CI](https://github.com/Cadiboo/NoCubes/workflows/Java%20CI/badge.svg?branch=master)
[
![Curseforge Downloads](http://cf.way2muchnoise.eu/full_nocubes_downloads.svg)
![Curseforge Versions](http://cf.way2muchnoise.eu/versions/nocubes.svg)
](https://www.curseforge.com/minecraft/mc-mods/nocubes)
[
![Discord](https://img.shields.io/discord/493715188843937793?label=Discord)
](https://discord.gg/NWzs34rqPB)  
A mod for 1.12.2+ by [Cadiboo](https://github.com/Cadiboo) that creates smooth terrain in Minecraft.  
[Website](https://Cadiboo.github.io/projects/nocubes/)  
[CurseForge Page](https://minecraft.curseforge.com/projects/nocubes)  
![NoCubes](https://cadiboo.github.io/projects/nocubes/sd-images/realistic.png "NoCubes")  

**This `master` branch is very in-dev and is subject to force-pushes**

### OptiFine compatibility
OptiFine is a bit hard to work with  
To get it running in your development environment:  
1. Download the OptiFine version that you want from [optifine.net](https://optifine.net/downloads)  
2. Download OptiFine dev tweaker from [GitHub](https://github.com/OpenCubicChunks/OptiFineDevTweaker/releases/latest)  
3. Put both jar files into `NoCubes/run/mods/`  
4. Run the game and OptiFine & Shaders will load

To be able to also compile against OptiFine (do the above steps first):
1. You must have set up NoCubes and had gradle install and deobfuscate Minecraft first
2. Make sure that you have Minecraft for the version you're running NoCubes on  
     If you don't, just open the minecraft launcher, select the right version and hit 'Play'.
     The launcher will download everything and you can just quit the game when it starts.
     To find the version of Minecraft that NoCubes is being built against look in `gradle.properties`.
3. Download OptiFineDeobf from [GitHub](https://github.com/Cadiboo/OptiFineDeobf/releases/latest)
4. Run the OptiFine jar in `NoCubes/run/mods/` and select 'Extract', put the resulting extracted jar somewhere outside the `mods` folder
5. Run OptiFineDeobf, select the extracted OptiFine jar in `NoCubes/run/mods/` and select the `NoCubes` folder for the project folder
6. Select a mappings file that maps from SRG to MCP/official (don't use obf -> MCP mappings)
7. Select 'Make Public' and 'Forge Dev jar' then click 'Deobf'
8. Put the deobfuscated OptiFine jar into `NoCubes/libs/`
9. Refresh gradle in your IDE and you should be able to compile against OptiFine's classes (and running OptiFine still works as it did before)

Note: You can replace the normal OptiFine jar with the extracted (non-deobfuscated) one in `NoCubes/run/mods/` (this *may* speed up OptiFine's loading time)

NoCubes' OptiFine compatibility should not be shipped to production compiled directly against OptiFine's classes because
it can crash if OptiFine isn't present. Instead, the code should be converted to Reflection.  
Using Reflection has the added benefit of allowing the same NoCubes jar to support multiple versions of OptiFine.

<details>
  <summary>Other OptiFine info (just putting this here so I don't forget and can come back to it at some point)</summary>

  It's possible to get Forge to do most of the work by adding the following code to `build.gradle`.
  ```groovy
  repositories {
    flatDir {
      dirs 'run/mods'
    }
  }
  dependencies {
    // OptiFine is the first dependency because so that we can compile against its version of vanilla's classes, not Forge's
    // Needs a group classifier even though it's not used, this can be anything, I used 'undefined'
    compileOnly fg.deobf('undefined:OptiFine_1.16.5_HD_U_G8_pre12_MOD.jar:')
    
    //... other dependencies like Forge
  }
  ```
  However, this doesn't fully work because Forge doesn't apply my ATs to OptiFine's classes :(  
  It's probably possible to run [AccessTransformers](https://github.com/MinecraftForge/ForgeGradle/blob/6639464b29b0923187eee0a609e546ba9f1b998b/src/userdev/java/net/minecraftforge/gradle/userdev/tasks/AccessTransformJar.java#L45) ourselves against the deobfed OptiFine dependency, but I can't figure out how   
</details>
