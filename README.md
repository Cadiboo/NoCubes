# [NoCubes Mod](https://Cadiboo.github.io/projects/nocubes/)
A mod for 1.12.2+ by [Cadiboo](https://github.com/Cadiboo) that creates smooth terrain in Minecraft.  
[Website](https://Cadiboo.github.io/projects/nocubes/)  
[CurseForge Page](https://minecraft.curseforge.com/projects/nocubes)  

# Devs - setting up
The integration with OptiFine makes this a bit more complicated than the usual Forge setup.
I forgot how to do this after moving to 1.16 for a while and wasn't able to get someone else set up with a dev environment...
I'm writing what got it to work for me when I returned to 1.12.2 here.
Steps:
1. Make sure you're using Java 8.
2. Comment out the OptiFine related line in the Access Transformer in `src/main/resources/META-INF/accesstransformer.cfg`
  - It has a comment above it about needing to be disabled
  - You can just open it in any text editor, it's not a special file
  - Comment the line out by putting `#` at the start of the line
3. Run the normal gradle setup commands (Make sure you're using Java 8)
  - `./gradlew setupDecompWorkspace && ./gradlew genIntellijRuns`
  - If you get an error saying "Your Access Transformers be broke!" you didn't do step 2 correctly
  - This has to download, deobfuscate, decompile and recompile Minecraft. It can take up to 20 minutes (but it only has to be done once) so go outside and run around or something.
4. Uncomment the OptiFine line in the Access Transformer file
5. Open the build.gradle file in IntelliJ (make sure you're using an older IntelliJ version that supports Gradle 2.14, I downloaded a 2018 version)
6. Download OptiFine from [here](https://optifine.net/downloads) (I used the latest non-prerelease version - `HD U F5` at the time)
7. Run OptiFine by double clicking it (Mac: if you get a dialog about it being from an unidentified developer open System Preferences and go to Security & Privacy, there should be a button at the bottom saying 'Open Anyway')
8. Click 'Extract' and put the resulting file in `./run/mods` (so `NoCubesProjectDirectory/run/mods`, create it if it doesn't exist)
9. Download OptiFineDevTweaker from [here](https://github.com/OpenCubicChunks/OptiFineDevTweaker/releases) and put it into `./run/mods/`
10. Thats as far as I got before I spent hours fruitlessly debugging graphics driver issues when trying to run OptiFine.
