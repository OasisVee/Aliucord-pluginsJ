/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;

import com.aliucord.Main;
import com.aliucord.Utils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Hook;
import com.aliucord.utils.DimenUtils;
import com.discord.databinding.WidgetUserSheetBinding;
import com.discord.models.user.CoreUser;
import com.discord.utilities.color.ColorCompat;
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage;
import com.discord.widgets.chat.list.entries.ChatListEntry;
import com.discord.widgets.chat.list.entries.MessageEntry;
import com.discord.widgets.user.usersheet.WidgetUserSheet;
import com.discord.widgets.user.usersheet.WidgetUserSheetViewModel;
import com.lytefast.flexinput.R;

import io.github.juby210.acplugins.pronoundb.*;

@AliucordPlugin
@SuppressLint("SetTextI18n")
@SuppressWarnings("unused")
public final class PronounDB extends Plugin {
    public PronounDB() {
        settingsTab = new SettingsTab(PluginSettings.class).withArgs(settings);
    }

    @Override
    public void start(Context context) {
        if (settings.getBool("displayChat", true)) {
            try {
                injectMessages();
            } catch (Throwable e) { 
                Main.logger.error("PronounDB failed to inject into messages", e); 
            }
        }
        
        if (settings.getBool("displayProfile", true)) {
            try {
                injectProfile();
            } catch (Throwable e) {
                Main.logger.error("PronounDB failed to inject into profile", e);
            }
        }
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }

    private void injectMessages() throws Throwable {
        var itemTimestampField = WidgetChatListAdapterItemMessage.class.getDeclaredField("itemTimestamp");
        itemTimestampField.setAccessible(true);

        patcher.patch(WidgetChatListAdapterItemMessage.class, "onConfigure", new Class<?>[]{ int.class, ChatListEntry.class }, new Hook(param -> {
            try {
                // Make sure we have a MessageEntry
                if (!(param.args[1] instanceof MessageEntry)) return;
                var messageEntry = (MessageEntry) param.args[1];
                var message = messageEntry.getMessage();
                if (message == null) return;
                
                // Get the timestamp view
                var itemTimestamp = (TextView) itemTimestampField.get(param.thisObject);
                if (itemTimestamp == null) return;

                var header = (ConstraintLayout) itemTimestamp.getParent();
                if (header == null) return;
                
                TextView pronounsView = header.findViewById(viewId);
                if (pronounsView == null) {
                    // Create the pronouns view
                    pronounsView = new TextView(header.getContext(), null, 0, R.i.UiKit_TextView);
                    pronounsView.setId(viewId);
                    pronounsView.setTextSize(12);
                    pronounsView.setTextColor(ColorCompat.getThemedColor(header.getContext(), R.b.colorTextMuted));
                    header.addView(pronounsView);

                    // Set up constraints
                    var set = new ConstraintSet();
                    set.clone(header);
                    set.constrainedHeight(viewId, true);
                    
                    // Find the username view ID
                    int nameId = Utils.getResId("chat_list_adapter_item_text_name", "id");
                    
                    // Connect to baseline of username
                    set.connect(viewId, ConstraintSet.BASELINE, nameId, ConstraintSet.BASELINE);
                    // Position after timestamp
                    set.connect(viewId, ConstraintSet.START, itemTimestamp.getId(), ConstraintSet.END);
                    set.connect(viewId, ConstraintSet.END, header.getId(), ConstraintSet.END);
                    // Make sure timestamp connects to our view
                    set.connect(itemTimestamp.getId(), ConstraintSet.END, viewId, ConstraintSet.START);
                    
                    // Apply constraints
                    set.applyTo(header);
                }

                // Get user info
                var user = new CoreUser(message.getAuthor());
                var bot = user.isBot();
                Long userId = user.getId();
                
                // Skip bots or already cached users
                if (!bot && !Store.cache.containsKey(userId)) {
                    // Fetch pronouns asynchronously
                    var finalPronounsView = pronounsView;
                    new Thread(() -> {
                        Store.fetchPronouns(userId);
                        new Handler(Looper.getMainLooper()).post(() -> {
                            try {
                                addPronounsToHeader(finalPronounsView, userId, false);
                            } catch (Throwable e) {
                                Main.logger.error("Failed to add pronouns to chat header", e);
                            }
                        });
                    }).start();
                } else {
                    try {
                        addPronounsToHeader(pronounsView, userId, bot);
                    } catch (Throwable e) {
                        Main.logger.error("Failed to add pronouns to chat header", e);
                    }
                }
            } catch (Throwable e) { 
                Main.logger.error("Error in onConfigure hook", e); 
            }
        }));
    }

    private void injectProfile() {
        patcher.patch(WidgetUserSheet.class, "configureNote", new Class<?>[]{ WidgetUserSheetViewModel.ViewState.Loaded.class }, new Hook(param -> {
            try {
                var state = (WidgetUserSheetViewModel.ViewState.Loaded) param.args[0];
                var user = state.getUser();
                if (user == null || user.isBot()) return;
                
                Long userId = user.getId();
                var sheet = (WidgetUserSheet) param.thisObject;
                
                // Get binding safely
                var binding = WidgetUserSheet.access$getBinding$p(sheet);
                if (binding == null || binding.a == null) return;
                
                if (!Store.cache.containsKey(userId)) {
                    // Fetch pronouns asynchronously
                    new Thread(() -> {
                        Store.fetchPronouns(userId);
                        new Handler(Looper.getMainLooper()).post(() -> {
                            try {
                                addPronounsToUserSheet(binding, userId);
                            } catch (Throwable e) {
                                Main.logger.error("Failed to add pronouns to user sheet", e);
                            }
                        });
                    }).start();
                } else {
                    try {
                        addPronounsToUserSheet(binding, userId);
                    } catch (Throwable e) {
                        Main.logger.error("Failed to add pronouns to user sheet", e);
                    }
                }
            } catch (Throwable e) {
                Main.logger.error("Error in configureNote hook", e);
            }
        }));
    }

    public void addPronounsToHeader(TextView pronounsView, Long userId, boolean bot) {
        String c;
        if (bot || (c = Store.cache.get(userId)) == null || c.equals("unspecified")) {
            pronounsView.setVisibility(View.GONE);
            return;
        }
        
        // Get pronouns string with proper format
        String pronounsText = Constants.getPronouns(c, settings.getInt("format", 0));
        if (pronounsText == null) {
            pronounsView.setVisibility(View.GONE);
            return;
        }
        
        pronounsView.setVisibility(View.VISIBLE);
        pronounsView.setText(" • " + pronounsText);
    }

    private static final int noteHeaderId = Utils.getResId("user_sheet_note_header", "id");

    public void addPronounsToUserSheet(WidgetUserSheetBinding binding, Long userId) {
        String c = Store.cache.get(userId);
        if (c == null || c.equals("unspecified")) return;
        
        // Get pronouns string with proper format
        String pronounsText = Constants.getPronouns(c, settings.getInt("format", 0));
        if (pronounsText == null) return;
        
        try {
            var noteHeader = binding.a.findViewById(noteHeaderId);
            if (noteHeader == null) return;
            
            var layout = (LinearLayout) noteHeader.getParent();
            if (layout == null) return;

            TextView pronounsView = layout.findViewById(viewId);
            if (pronounsView == null) {
                pronounsView = new TextView(layout.getContext(), null, 0, R.i.UserProfile_Section_Header);
                pronounsView.setId(viewId);
                pronounsView.setTypeface(ResourcesCompat.getFont(layout.getContext(), com.aliucord.Constants.Fonts.whitney_semibold));
                pronounsView.setPadding(DimenUtils.dpToPx(16), 0, 0, 0);
                
                // Insert before note header
                int index = layout.indexOfChild(noteHeader);
                if (index >= 0) {
                    layout.addView(pronounsView, index);
                } else {
                    layout.addView(pronounsView);
                }
            }
            
            pronounsView.setText("Pronouns • " + pronounsText);
            pronounsView.setVisibility(View.VISIBLE);
        } catch (Throwable e) {
            Main.logger.error("Error adding pronouns to user sheet", e);
        }
    }

    public final int viewId = View.generateViewId();
}
