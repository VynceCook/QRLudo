package fr.angers.univ.qrludo.utils;

import java.io.IOException;

public interface UrlContentCallback {
        void onWebsiteContent(String content) throws IOException;
}
