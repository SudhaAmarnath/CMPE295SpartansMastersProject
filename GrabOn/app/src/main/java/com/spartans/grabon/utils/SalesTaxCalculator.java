package com.spartans.grabon.utils;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Author : Sudha Amarnath on 2020-04-06
 */
public class SalesTaxCalculator {

    private FirebaseFirestore db = Singleton.getDb();;
    private static String state = null;

    public double getSalesTax(String state) {

        double taxrate = 0;

        switch(state) {
            case "AL":
                taxrate = 13.5;
                break;
            case "AK":
                taxrate = 7.0;
                break;
            case "AZ":
                taxrate = 10.725;
                break;
            case "AR":
                taxrate = 11.625;
                break;
            case "CA":
                taxrate = 10.5;
                break;
            case "CO":
                taxrate = 10.0;
                break;
            case "CT":
                taxrate = 6.35;
                break;
            case "DE":
                taxrate = 0.0;
                break;
            case "FL":
                taxrate = 7.5;
                break;
            case "GA":
                taxrate = 8.0;
                break;
            case "HI":
                taxrate = 4.712;
                break;
            case "ID":
                taxrate = 8.5;
                break;
            case "IL":
                taxrate = 10.25;
                break;
            case "IN":
                taxrate = 7.0;
                break;
            case "IA":
                taxrate = 7.0;
                break;
            case "KS":
                taxrate = 11.5;
                break;
            case "KY":
                taxrate = 6.0;
                break;
            case "LA":
                taxrate = 11.45;
                break;
            case "ME":
                taxrate = 5.5;
                break;
            case "MD":
                taxrate = 6.0;
                break;
            case "MA":
                taxrate = 6.25;
                break;
            case "MI":
                taxrate = 6.0;
                break;
            case "MN":
                taxrate = 7.875;
                break;
            case "MS":
                taxrate = 7.25;
                break;
            case "MO":
                taxrate = 10.85;
                break;
            case "MT":
                taxrate = 0.0;
                break;
            case "NE":
                taxrate = 7.5;
                break;
            case "NV":
                taxrate = 8.25;
                break;
            case "NH":
                taxrate = 0.0;
                break;
            case "NJ":
                taxrate = 12.625;
                break;
            case "NM":
                taxrate = 8.688;
                break;
            case "NY":
                taxrate = 8.875;
                break;
            case "NC":
                taxrate = 7.5;
                break;
            case "ND":
                taxrate = 8.0;
                break;
            case "OH":
                taxrate = 8.0;
                break;
            case "OK":
                taxrate = 11.0;
                break;
            case "OR":
                taxrate = 0.0;
                break;
            case "PA":
                taxrate = 8.0;
                break;
            case "RI":
                taxrate = 7.0;
                break;
            case "SC":
                taxrate = 9.0;
                break;
            case "SD":
                taxrate = 6.0;
                break;
            case "TN":
                taxrate = 9.75;
                break;
            case "TX":
                taxrate = 8.25;
                break;
            case "UT":
                taxrate = 8.35;
                break;
            case "VT":
                taxrate = 7.0;
                break;
            case "VA":
                taxrate = 6.0;
                break;
            case "WA":
                taxrate = 10.4;
                break;
            case "WV":
                taxrate = 7.0;
                break;
            case "WI":
                taxrate = 6.75;
                break;
            case "WY":
                taxrate = 6.0;
                break;
            default:
                taxrate = 0.0;
                break;
        }

        return taxrate;
    }

}
