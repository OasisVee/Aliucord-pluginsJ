package io.github.juby210.acplugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliucord.Utils;
import com.aliucord.api.SettingsAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.widgets.BottomSheet;
import com.aliucord.widgets.CheckedSetting;
import com.aliucord.widgets.RadioManager;
import com.discord.models.guild.GuildMember;
import com.discord.models.user.User;
import com.discord.stores.StoreStream;
import com.discord.widgets.chat.list.entries.WidgetChatListAdapterItemEmbed;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class CustomNicknameFormat extends Plugin {
    public CustomNicknameFormat() {
        settingsTab = new SettingsTab(PluginSettings.class, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings);
    }

    public static class PluginSettings extends BottomSheet {
        public int getContentViewResId() { return 0; }

        private final SettingsAPI settings;

        public PluginSettings(SettingsAPI settings) {
            this.settings = settings;
        }

        @Nullable
        @Override
        @SuppressLint("SetTextI18n")
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            var context = inflater.getContext();
            var layout = new LinearLayout(context);
            layout.setBackgroundColor(Utils.getColor(context, com.lytefast.flexinput.R.c.colorBackgroundPrimary));

            var radios = Arrays.asList(
                Utils.createCheckedSetting(context, CheckedSetting.ViewType.RADIO, "Nickname (Username)", null),
                Utils.createCheckedSetting(context, CheckedSetting.ViewType.RADIO, "Nickname (Tag)", null),
                Utils.createCheckedSetting(context, CheckedSetting.ViewType.RADIO, "Username", null),
                Utils.createCheckedSetting(context, CheckedSetting.ViewType.RADIO, "Username (Nickname)", null),
                Utils.createCheckedSetting(context, CheckedSetting.ViewType.RADIO, "Display Name (Username)", null)
            );

            var radioManager = new RadioManager(radios);
            var format = Format.valueOf(settings.getString("format", Format.NICKNAME_USERNAME.name()));

            var j = radios.size();
            for (var i = 0; i < j; i++) {
                var k = i;
                var radio = radios.get(k);
                radio.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        settings.setString("format", Format.values()[k].name());
                        radioManager.a(radio);
                    }
                });
                layout.addView(radio);
                if (k == format.ordinal()) radioManager.a(radio);
            }

            return layout;
        }
    }

    public enum Format {NICKNAME_USERNAME, NICKNAME_TAG, USERNAME, USERNAME_NICKNAME, DISPLAYNAME_USERNAME}

    @Override
    public void start(Context context) throws Throwable {
        patcher.patch(
            GuildMember.class.getDeclaredMethod("getNickOrUsername", User.class, GuildMember.class, com.discord.api.channel.Channel.class, List.class),
            new Hook(param -> {
                var user = (User) param.args[0];
                var username = user.getUsername();
                var displayName = user.getDisplayName();
                var res = (String) param.getResult();
                if (res.equals(username)) return;

                param.setResult(getFormatted(username, displayName, res, user));
            })
        );

        // fix custom format in embeds
        patcher.patch(
            WidgetChatListAdapterItemEmbed.Companion.getModel$1.class.getDeclaredMethod("call", Object.class, Object.class),
            new Hook(param -> {
                var map = (Map<Long, String>) param.getResult();
                if (map.size() == 0) return;
                var users = StoreStream.getUsers().getUsers();
                for (var entry : map.entrySet()) {
                    var id = entry.getKey();
                    var user = users.get(id);
                    if (user != null) entry.setValue(getFormatted(user.getUsername(), user.getDisplayName(), entry.getValue(), user));
                }
            })
        );
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }

    private String getFormatted(String username, String displayName, String res, User user) {
        var format = Format.valueOf(settings.getString("format", Format.NICKNAME_USERNAME.name()));
        switch (format) {
            case NICKNAME_USERNAME:
                return res + " (" + username + ")";
            case NICKNAME_TAG:
                return res + " (" + username + UserUtils.INSTANCE.getDiscriminatorWithPadding(user) + ")";
            case USERNAME:
                return username;
            case USERNAME_NICKNAME:
                return username + " (" + res + ")";
            case DISPLAYNAME_USERNAME:
                return displayName + " (" + username + ")";
        }
        return res;
    }
}
