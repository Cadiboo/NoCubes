package clickme.nocubes;

import java.net.*;
import com.google.common.io.*;
import java.util.*;
import com.google.gson.*;
import java.io.*;
import cpw.mods.fml.common.versioning.*;

class NoCubes$1 extends Thread {
    @Override
    public void run() {
        try {
            final URL url = new URL("https://dl.dropboxusercontent.com/u/71419016/nc/promotions.json");
            final InputStream input = url.openStream();
            final String data = new String(ByteStreams.toByteArray(input));
            input.close();
            final Map<String, Object> json = (Map<String, Object>)new Gson().fromJson(data, (Class)Map.class);
            final Map<String, String> promos = json.get("promos");
            final String lat = promos.get("1.7.10-latest");
            final ArtifactVersion current = (ArtifactVersion)new DefaultArtifactVersion("1.0");
            if (lat != null) {
                final ArtifactVersion latest = (ArtifactVersion)new DefaultArtifactVersion(lat);
                if (latest.compareTo((Object)current) > 0) {
                    NoCubes.access$002(NoCubes.this, true);
                }
            }
        }
        catch (IOException e) {}
        catch (JsonSyntaxException ex) {}
    }
}