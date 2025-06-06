/*
 * Copyright (c) 2021-2022 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins.messagelogger;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import com.aliucord.Utils;
import com.aliucord.fragments.ConfirmDialog;
import com.aliucord.views.Button;
import com.aliucord.views.DangerButton;
import com.aliucord.widgets.BottomSheet;
import com.discord.views.CheckedSetting;
import com.lytefast.flexinput.R;
import io.github.juby210.acplugins.messagelogger.*;

class PluginSettings : BottomSheet() {
    private var isWhitelist = false;
    private var ignoreMutedServers = false;
    private var isChannelWhitelist = false;
    private var ignoreMutedChannels = false;
    private var alwaysLogSelected = false;
    private var logEdits = false;
    private var logDeletes = false;
    private var saveLogs = false;

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle);

        val context = requireContext();
        with(SQLite(context)) {
            isWhitelist = getBoolSetting("whitelist", false);
            ignoreMutedServers = getBoolSetting("ignoreMutedServers", true);
            isChannelWhitelist = getBoolSetting("channelWhitelist", false);
            ignoreMutedChannels = getBoolSetting("ignoreMutedChannels", true);
            alwaysLogSelected = getBoolSetting("alwaysLogSelected", true);
            logEdits = getBoolSetting("logEdits", true);
            logDeletes = getBoolSetting("logDeletes", true);
            saveLogs = getBoolSetting("saveLogs", true);
            close();
        }

        setPadding(20);
        addView(TextView(context, null, 0, R.i.UiKit_Settings_Item_Header).apply {
            text = "Message Logger";
            gravity = Gravity.START;
        });

        addView(TextView(context, null, 0, R.i.UiKit_Settings_Item_Header).apply {
            text = "Logging";
            gravity = Gravity.CENTER_HORIZONTAL;
        });

        addView(Button(context).apply {
            text = "Toggle Whitelist / Blacklist For Servers (Current Is " + (if (isWhitelist) "Whitelist" else "Blacklist") + ")";
            setOnClickListener {
                isWhitelist = !isWhitelist;
                with(SQLite(context)) {
                    setBoolSetting("whitelist", isWhitelist);
                    close();
                }
                Utils.showToast("Servers will now need to be " + if (isWhitelist) "whitelisted" else "blacklisted");
                dismiss();
            }
        });

        addView(Utils.createCheckedSetting(context, CheckedSetting.ViewType.CHECK, "Ignore Muted Servers", null).apply {
            isChecked = ignoreMutedServers;
            setOnCheckedListener {
                ignoreMutedServers = !ignoreMutedServers;
                with(SQLite(context)) {
                    setBoolSetting("ignoreMutedServers", ignoreMutedServers);
                    close();
                }
                Utils.showToast("Muted servers will now be " + (if (ignoreMutedServers) "" else "not ") + "ignored");
            }
        });

        addView(Button(context).apply {
            text = "Toggle Whitelist / Blacklist For Channels (Current Is " + (if (isChannelWhitelist) "Whitelist" else "Blacklist") + ")";
            setOnClickListener {
                isChannelWhitelist = !isChannelWhitelist;
                with(SQLite(context)) {
                    setBoolSetting("channelWhitelist", isChannelWhitelist);
                    close();
                }
                Utils.showToast("Channels will now need to be " + if (isChannelWhitelist) "whitelisted" else "blacklisted");
                dismiss();
            }
        });

        addView(Utils.createCheckedSetting(context, CheckedSetting.ViewType.CHECK, "Ignore Muted Channels", null).apply {
            isChecked = ignoreMutedChannels;
            setOnCheckedListener {
                ignoreMutedChannels = !ignoreMutedChannels;
                with(SQLite(context)) {
                    setBoolSetting("ignoreMutedChannels", ignoreMutedChannels);
                    close();
                }
                Utils.showToast("Muted channels will now be " + (if (ignoreMutedChannels) "" else "not ") + "ignored");
            }
        });

        addView(Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Always Log Selected", "Always log selected server regardless of whitelist/blacklist").apply {
            isChecked = alwaysLogSelected;
            setOnCheckedListener {
                alwaysLogSelected = !alwaysLogSelected;
                with(SQLite(context)) {
                    setBoolSetting("alwaysLogSelected", alwaysLogSelected);
                    close();
                }
            }
        });

        addView(Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Log Edits", "Log edited messages").apply {
            isChecked = logEdits;
            setOnCheckedListener {
                logEdits = !logEdits;
                with(SQLite(context)) {
                    setBoolSetting("logEdits", logEdits);
                    close();
                }
                Utils.showToast(if (logEdits) "Now logging edited messages" else "No longer logging edited messages");
            }
        });

        addView(Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Log Deletes", "Log deleted messages").apply {
            isChecked = logDeletes;
            setOnCheckedListener {
                logDeletes = !logDeletes;
                with(SQLite(context)) {
                    setBoolSetting("logDeletes", logDeletes);
                    close();
                }
                Utils.showToast(if (logDeletes) "Now logging deleted messages" else "No longer logging deleted messages");
            }
        });

        addView(Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Save Logs", "Save logs to the database").apply {
            isChecked = saveLogs;
            setOnCheckedListener {
                saveLogs = !saveLogs;
                with(SQLite(context)) {
                    setBoolSetting("saveLogs", saveLogs);
                    close();
                }
                Utils.showToast("Logs will be now " + (if (saveLogs) "" else "not ") + "saved");
            }
        });

        addView(TextView(context, null, 0, R.i.UiKit_Settings_Item_Header).apply {
            text = "Database";
            gravity = Gravity.CENTER_HORIZONTAL;
        });

        addView(Button(context).apply {
            text = "Export Database";
            setOnClickListener {
                with(SQLite(context)) {
                    exportDatabase();
                    close();
                }
            }
        });

        addView(Button(context).apply {
            text = "Import Database (Requires Restart)";
            setOnClickListener {
                with(SQLite(context)) {
                    importDatabase();
                    close();
                }
                RestartUtils.promptRestart(requireContext(), "Database imported. Restart to apply changes?");
            }
        });

        addView(DangerButton(context).apply {
            text = "Clear Edit Logs (Requires Restart)";
            setOnClickListener {
                val confirmDelete = ConfirmDialog()
                    .setTitle("Clear All Edited Messages?")
                    .setIsDangerous(true)
                    .setDescription("Are you sure you want to clear all edited messages from the logs?");
                confirmDelete.setOnOkListener {
                    with(SQLite(context)) {
                        clearEditedMessages();
                        close();
                    }
                    RestartUtils.promptRestart(requireContext(), "Edit logs cleared. Restart to apply changes?");
                    confirmDelete.dismiss();
                    dismiss();
                }.show(parentFragmentManager, "ClearEditedMessages");
            }
        });

        addView(DangerButton(context).apply {
            text = "Clear Delete Logs (Requires Restart)";
            setOnClickListener {
                val confirmDelete = ConfirmDialog()
                    .setTitle("Clear All Deleted Messages?")
                    .setIsDangerous(true)
                    .setDescription("Are you sure you want to clear all deleted messages from the logs?");
                confirmDelete.setOnOkListener {
                    with(SQLite(context)) {
                        clearDeletedMessages();
                        close();
                    }
                    RestartUtils.promptRestart(requireContext(), "Delete logs cleared. Restart to apply changes?");
                    confirmDelete.dismiss();
                    dismiss();
                }.show(parentFragmentManager, "ClearDeletedMessages");
            }
        });

        addView(DangerButton(context).apply {
            text = "Clear All Logs (Requires Restart)";
            setOnClickListener {
                val confirmDelete = ConfirmDialog()
                    .setTitle("Clear All Message Logs?")
                    .setIsDangerous(true)
                    .setDescription("Are you sure you want to clear ALL edited and deleted messages from the logs?");
                confirmDelete.setOnOkListener {
                    with(SQLite(context)) {
                        clearEditedMessages();
                        clearDeletedMessages();
                        close();
                    }
                    RestartUtils.promptRestart(requireContext(), "All logs cleared. Restart to apply changes?");
                    confirmDelete.dismiss();
                    dismiss();
                }.show(parentFragmentManager, "ClearAllLogs");
            }
        });

        addView(DangerButton(context).apply {
            text = "Clear Server " + (if (isWhitelist) "Whitelist" else "Blacklist") + " (Requires Restart)";
            setOnClickListener {
                val confirmDelete = ConfirmDialog()
                    .setTitle("Clear Server " + if (isWhitelist) "Whitelist" else "Blacklist")
                    .setIsDangerous(true)
                    .setDescription("Are you sure you want to clear all " + (if (isWhitelist) "whitelisted" else "blacklisted") + " servers?");
                confirmDelete.setOnOkListener {
                    with(SQLite(context)) {
                        clearGuilds();
                        close();
                    }
                    RestartUtils.promptRestart(requireContext(), "Server list cleared. Restart to apply changes?");
                    confirmDelete.dismiss();
                    dismiss();
                }.show(parentFragmentManager, "ClearGuilds");
            }
        });

        addView(DangerButton(context).apply {
            text = "Clear Channel " + (if (isChannelWhitelist) "Whitelist" else "Blacklist") + " (Requires Restart)";
            setOnClickListener {
                val confirmDelete = ConfirmDialog()
                    .setTitle("Clear Channel " + if (isChannelWhitelist) "Whitelist" else "Blacklist")
                    .setIsDangerous(true)
                    .setDescription("Are you sure you want to clear all " + (if (isChannelWhitelist) "whitelisted" else "blacklisted") + " channels?");
                confirmDelete.setOnOkListener {
                    with(SQLite(context)) {
                        clearChannels();
                        close();
                    }
                    RestartUtils.promptRestart(requireContext(), "Channel list cleared. Restart to apply changes?");
                    confirmDelete.dismiss();
                    dismiss();
                }.show(parentFragmentManager, "ClearChannels");
            }
        });
    }
}
