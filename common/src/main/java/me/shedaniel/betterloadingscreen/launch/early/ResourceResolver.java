package me.shedaniel.betterloadingscreen.launch.early;

import java.io.InputStream;

public interface ResourceResolver {
    InputStream resolve(String url);
}
