package fr.angers.univ.qrludo.utils;

import android.util.Base64;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public class DecompressionJSON {

    public static String decompresser(String zipString) throws IOException {
        //zipString = "H4sIAAAAAAAAA41TMY7bMBD8ykYp7AMUA4fr3KQ4pAlwRXJAmjgFLa19NMilzCUNHIL8JV3gd+hjmZXOSptOJHdnZ2ZHPxtxkZttU1hL0zbldbBTFX+ujHPvimu235tHpnOmLvVMmYfM0lNJQtH5QNs7FD67UEu7k88r5wkFin46ePGEluN4zZwpMJ0UXVrxvXLDEDzFtPe4H5LdsdKXr/SIKUosynGPJy7kSopGqKW+gkQd6LRyF+eVhlDVc81KeNbik2x38q3aiYKj7sV5AYTMg5cpb1p0QVYbs4xMUqw7cmTSkmtXasZwDPjQMV2y85FRIuO1Y1XnM5vKw/Txj1EXnCqQ112KAAJhKXfUO1FzYvLSSZ+T72mtDlQBabyrLF7f6NHAFTZUVY8HKSw+k/GvOWhLWMLBoeBUtYAf3PYHPxs+V1fjB0V7F7yCLLY683m7D2B+t6FnoDOOsoJUGpz+n8SWTsYQ7k38xz+O1Eds10ggDscMnTxZdhO+NvA9a/JiuGApzhdgo8AyaJfnPJWOv41hP14Ld7bgDT1hlZCbeVm6AXScsWrkKhyTtaMDxlsU1Sr6OXwtlTrlIFt8ojWwXNIr3JrSYQJOq0nyRAJ8IhebhRANwWH/H+nTbRdxaa4yC7uNHK+3rG528sS58/Su+dE2XQop4w97f+juH+4fml9/AW/lfLyCAwAA";
        byte[] decode = Base64.decode(zipString,Base64.DEFAULT); // décode le texte compressé en byte[]

        ByteArrayInputStream bis = new ByteArrayInputStream(decode);
        GZIPInputStream gis = new GZIPInputStream(bis);
        BufferedReader br = new BufferedReader(new InputStreamReader(gis,"UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        gis.close();
        bis.close();
        System.out.println(sb.toString());
        return sb.toString();
    }

}
