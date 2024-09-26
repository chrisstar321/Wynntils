/*
 *  * Copyright © Wynntils - 2018 - 2022.
 */

package com.wynntils.modules.chat.managers;

import com.wynntils.core.framework.FrameworkManager;
import com.wynntils.modules.chat.ChatModule;
import com.wynntils.modules.chat.configs.ChatConfig;
import com.wynntils.modules.chat.events.custom.ChatTabsUpdatedEvent;
import com.wynntils.modules.chat.instances.ChatTab;
import com.wynntils.modules.chat.overlays.gui.ChatGUI;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TabManager {

    public static final String DEFAULT_GUILD_REGEX = "(^&3\\[((&b★{0,5})|(&b.*))?(&3)?(&o)?[\\w ]*?(&3)?\\])(?<!&3\\[Parkour\\])|(^&3You were not in the territory)";
    public static final String DEFAULT_PARTY_REGEX = "(^&7\\[&r&e(&o)?[a-zA-Z0-9_ ]+?&r&7\\])|(^&eYou are not in a party!)";

    private static List<ChatTab> availableTabs;

    public static void startTabs() {
        availableTabs = ChatConfig.INSTANCE.available_tabs;

        if (!ChatConfig.INSTANCE.registeredDefaultTabs) {
            availableTabs.add(new ChatTab("All", ".*", null, "", false, 0));

            ChatConfig.INSTANCE.registeredDefaultTabs = true;
            Collections.sort(availableTabs);
            ChatConfig.INSTANCE.saveSettings(ChatModule.getModule());
        }

        availableTabs.forEach(chatTab -> {
            // Replace old Party regex to not match champion (12 feb 2021)
            if (chatTab.getRegex().contains("(^&7\\[&r&e(.*?)\\])|(^&eYou are not in a party!)")) {
                chatTab.setRegex(chatTab.getRegex().replace("(^&7\\[&r&e(.*?)\\])|(^&eYou are not in a party!)", DEFAULT_PARTY_REGEX));
            }
            if (chatTab.getRegex().contains("(^&3\\[(&r&b★{0,2})?&r&3\\w*?\\])(?<!&3\\[Parkour\\])|(^&3You were not in the territory)")) {
                chatTab.setRegex(chatTab.getRegex().replace("(^&3\\[(&r&b★{0,2})?&r&3\\w*?\\])(?<!&3\\[Parkour\\])|(^&3You were not in the territory)", "(^&3\\[(&r&b★{0,4})?&r&3[\\w ]*?\\])(?<!&3\\[Parkour\\])|(^&3You were not in the territory)"));
            }
            if (chatTab.getRegex().contains("(^&3\\[(&r&b★{0,4})?&r&3[\\w ]*?\\])(?<!&3\\[Parkour\\])|(^&3You were not in the territory)")) {
                chatTab.setRegex(chatTab.getRegex().replace("(^&3\\[(&r&b★{0,4})?&r&3\\w*?\\])(?<!&3\\[Parkour\\])|(^&3You were not in the territory)", "(^&3\\[(&r&b★{0,4})?&r&3(&o)?[\\w ]*?(&r&3)?\\])(?<!&3\\[Parkour\\])|(^&3You were not in the territory)"));
            }
            if (chatTab.getRegex().contains("(^&3\\[(&r&b★{0,4})?&r&3(&o)?[\\w ]*?(&r&3)?\\])(?<!&3\\[Parkour\\])|(^&3You were not in the territory)")) {
                chatTab.setRegex(chatTab.getRegex().replace("(^&3\\[(&r&b★{0,4})?&r&3(&o)?[\\w ]*?(&r&3)?\\])(?<!&3\\[Parkour\\])|(^&3You were not in the territory)", "(^&3\\[(&r&b★{0,5})?&r&3(&o)?[\\w ]*?(&r&3)?\\])(?<!&3\\[Parkour\\])|(^&3You were not in the territory)"));
            }
        });
    }

    public static void registerPresets() {
        if (availableTabs != null) {
            int oldTabs = availableTabs.size();

            switch (ChatConfig.INSTANCE.preset) {
                case a:
                    availableTabs.add(new ChatTab("Global", ".*", null, "", true, 0));
                    availableTabs.add(new ChatTab("Guild", DEFAULT_GUILD_REGEX, null, "/g", false, 1));
                    availableTabs.add(new ChatTab("Party", DEFAULT_PARTY_REGEX, null, "/p", false, 2));
                    break;
                case b:
                    availableTabs.add(new ChatTab("Global", ".*", null, "", true, 0));
                    availableTabs.add(new ChatTab("Shouts", "^&3.*shouts:", null, "", false, 1));
                    availableTabs.add(new ChatTab("G/P", DEFAULT_GUILD_REGEX + "|" + DEFAULT_PARTY_REGEX, null, "/g", false, 2));
                    availableTabs.add(new ChatTab("Private", "&7\\[.*\u27A4.*&7\\]", null, "/r", false, 3));
                    break;
                case c:
                    availableTabs.add(new ChatTab("All", ".*", null, "", true, 0));
                    availableTabs.add(new ChatTab("Guild", DEFAULT_GUILD_REGEX, null, "/g", true, 1));
                    availableTabs.add(new ChatTab("Party", DEFAULT_PARTY_REGEX, null, "/p", true, 2));
                    availableTabs.add(new ChatTab("Bombs", "&e\\[Bomb Bell\\]", null, "/switch", true, 3));
                    availableTabs.add(new ChatTab("Global", "^&[a78]\\[.+\\*?\\/\\w{2}", null, "", true, 4));
                    availableTabs.add(new ChatTab("Local", "^&[a7]\\[.+\\*?\\/\\w{2}", null, "", true, 5));
                    availableTabs.add(new ChatTab("Private", "&7\\[.*\u27A4.*&7\\]", null, "/r", true, 6));
                    availableTabs.add(new ChatTab("Shouts", "(^&3.*shouts:)", null, "", true, 7));
                    break;
                case vanilla:
                    availableTabs.add(new ChatTab("All", ".*", null, "", false, 0));
                    break;
            }

            for (int i = 0; i < oldTabs; i++) {
                deleteTab(0);
            }

            Collections.sort(availableTabs);
            saveConfigs();
        }
    }

    public static void registerNewTab(ChatTab tab) {
        availableTabs.add(tab);
        Collections.sort(availableTabs);
        FrameworkManager.getEventBus().post(new ChatTabsUpdatedEvent.TabAdded(tab));
        saveConfigs();
    }

    public static int deleteTab(int id) {
        if (id >= availableTabs.size()) return 0;

        ChatTab removed = availableTabs.get(id);
        availableTabs.remove(id);
        FrameworkManager.getEventBus().post(new ChatTabsUpdatedEvent.TabAdded(removed));
        saveConfigs();
        return id == 0 ? 0 : id - 1;
    }

    public static List<ChatTab> getAvailableTabs() {
        return availableTabs;
    }

    public static ChatTab getTabById(int id) {
        if (availableTabs.size() <= id || availableTabs.get(id) == null) return getTabById(id-1);
        return availableTabs.get(id);
    }

    public static void updateTab(int id, String name, String regex, Map<String, Boolean> regexSettings, String autoCommand, boolean lowPriority, int orderNb) {
        availableTabs.get(id).update(name, regex.replace("&", "§"), regexSettings, autoCommand, lowPriority, orderNb);
        Collections.sort(availableTabs);
        FrameworkManager.getEventBus().post(new ChatTabsUpdatedEvent.TabAdded(availableTabs.get(id)));
        saveConfigs();
    }

    private static void saveConfigs() {
        ChatConfig.INSTANCE.available_tabs = availableTabs;
        ChatConfig.INSTANCE.saveSettings(ChatModule.getModule());
    }
}
