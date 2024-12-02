package com.example.tundra_snow_app.Helpers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Utility class for generating Identicons, which are visual representations
 * derived from hash values. Identicons are commonly used for user avatars
 * when no profile image is provided.
 */
public class IdenticonGenerator {

    /**
     * Generates an Identicon Bitmap based on a hash value.
     *
     * @param hash   The hash value to base the Identicon on.
     * @param size   The size of the Identicon in pixels (e.g., 256x256).
     * @return A Bitmap of the generated Identicon.
     */
    public static Bitmap generateIdenticon(int hash, int size) {
        // Generate a 5x5 grid Identicon
        int gridSize = 5;
        int cellSize = size / gridSize;

        Bitmap identicon = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(identicon);
        Paint paint = new Paint();

        // Extract colors from the hash
        int r = (hash >> 16) & 0xFF;
        int g = (hash >> 8) & 0xFF;
        int b = hash & 0xFF;
        paint.setColor(Color.rgb(r, g, b));

        // Generate the grid pattern
        boolean[][] grid = generatePattern(hash);

        // Draw the Identicon
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                if (grid[x][y]) {
                    int left = x * cellSize;
                    int top = y * cellSize;
                    int right = left + cellSize;
                    int bottom = top + cellSize;
                    canvas.drawRect(left, top, right, bottom, paint);
                }
            }
        }

        return identicon;
    }

    /**
     * Generates a 5x5 boolean grid based on the hash value.
     *
     * @param hash The hash value.
     * @return A 5x5 boolean grid.
     */
    private static boolean[][] generatePattern(int hash) {
        boolean[][] grid = new boolean[5][5];

        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 3; x++) {
                boolean value = ((hash >> (y * 5 + x)) & 1) == 1;
                grid[x][y] = value;
                grid[4 - x][y] = value; // Mirror the left side to the right
            }
        }

        return grid;
    }
}
