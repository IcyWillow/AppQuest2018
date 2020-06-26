package ch.hsr.appquest.coincollector;

import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;

public class RegionIdentifier {

    public static int GetRegionPhotoFile(int majorNumber) {

        int region1 = R.drawable.lakeside_coin;
        int region2 = R.drawable.island_coin;
        int region3 = R.drawable.cafeteria_coin;
        int region4 = R.drawable.bicyclestand_coin;
        int region5 = R.drawable.researchbuilding_coin;

        switch (majorNumber) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return region1;
            case 6:
            case 7:
            case 8:
                return region2;
            case 9:
            case 10:
            case 11:
                return region3;
            case 12:
            case 13:
                return region4;
            case 14:
            case 15:
                return region5;

        }
        return R.drawable.sample_coin;
    }

    public static String getRegionName(int majorNumber) {

        String region1 = "Seeufer";
        String region2 = "Inseli";
        String region3 = "Mensa";
        String region4 = "Veloständer";
        String region5 = "Forschungsgebäude";

        switch (majorNumber) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return region1;
            case 6:
            case 7:
            case 8:
                return region2;
            case 9:
            case 10:
            case 11:
                return region3;
            case 12:
            case 13:
                return region4;
            case 14:
            case 15:
                return region5;

        }
        return "";
    }
}
