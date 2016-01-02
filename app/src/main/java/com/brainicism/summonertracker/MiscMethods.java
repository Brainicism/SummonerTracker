package com.brainicism.summonertracker;

/**
 * Created by Brian on 12/31/2015.
 */
public class MiscMethods {
    public static String normalizeQueueType(String raw){
        switch (raw){
            case "CUSTOM":
                return "Custom";
            case "NORMAL_3x3":
                return "Normal 3v3";
            case "NORMAL_5x5_BLIND":
                return "Normal 5v5 (Blind Pick)";
            case "NORMAL_5x5_DRAFT":
                return "Normal 5v5 (Draft Pick)";
            case "RANKED_SOLO_5x5":
                return "Ranked Solo 5v5";
            case "RANKED_TEAM_3x3":
                return "Ranked Team 3v3";
            case "RANKED_TEAM_5x5":
                return "Ranked Team 5v5";
            case "ODIN_5x5_BLIND":
                return "Dominion 5v5 (Blind Pick)";
            case "ODIN_5x5_DRAFT":
                return "Dominion 5v5 (Draft Pick)";
            case "BOT_ODIN_5x5":
                return "Bots";
            case "BOT_5x5_INTRO":
                return "Bots";
            case "BOT_5x5_BEGINNER":
                return "Bots";
            case "BOT_5x5_INTERMEDIATE":
                return "Bots";
            case "BOT_TT_3x3":
                return "Bots";
            case "GROUP_FINDER_5x5":
                return "Team Builder 5v5";
            case "ARAM_5x5":
                return "ARAM";
            case "ONEFORALL_5x5":
                return "One For All";
            case "FIRSTBLOOD_1x1":
                return "Snowdown Showdown (1v1)";
            case "FIRSTBLOOD_2x2":
                return "Snowdown Showdown (2v2)";
            case "SR_6x6":
                return "Hexakill";
            case "URF_5x5":
                return "URF";
            case "BOT_URF_5x5":
                return "URF Bots";
            case "NIGHTMARE_BOT_5x5_RANK1":
                return "Nightmare Bots";
            case "NIGHTMARE_BOT_5x5_RANK2":
                return "Nightmare Bots";
            case "NIGHTMARE_BOT_5x5_RANK5":
                return "Nightmare Bots";
            case "ASCENSION_5x5":
                return "Ascension";
            case "HEXAKILL":
                return "Hexakill";
            case "BILGEWATER_ARAM_5x5":
                return "Bilgewater Aram";
            case "KING_PORO_5x5":
                return "Legend of the Poro King";
            case "COUNTER_PICK":
                return "Nemesis";
            case "BILGEWATER_5x5":
                return "Black Market Brawlers";
            default:
                return "Unknown";

        }
    }
}
