package com.walletv2.entity;

import com.walletv2.activity.R;

public class RandomColor {

    public static int getRandomColor(String name) {
        int color = 0;
        for (char c : name.toCharArray()) {
            color += c;
        }

        return getColor(color % 10);
    }

    private static int getColor(int random) {
        switch (random) {
            case 0:
                return R.drawable.holo_blue_bright_circle;
            case 1:
                return R.drawable.holo_blue_dark_circle;
            case 2:
                return R.drawable.holo_blue_light_circle;
            case 3:
                return R.drawable.holo_green_dark_circle;
            case 4:
                return R.drawable.holo_green_light_circle;
            case 5:
                return R.drawable.holo_orange_dark_circle;
            case 6:
                return R.drawable.holo_orange_light_circle;
            case 7:
                return R.drawable.holo_purple_circle;
            case 8:
                return R.drawable.holo_red_dark_circle;
            case 9:
                return R.drawable.holo_red_light_circle;
            default:
                return R.drawable.darker_gray_circle;
        }
    }
}
