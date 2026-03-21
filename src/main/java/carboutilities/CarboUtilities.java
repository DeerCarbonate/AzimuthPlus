package carboutilities;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.util.Log;
import carboutilities.ui.CircleResearchDialog;
import mindustry.Vars;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.gen.Icon;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable;

public class CarboUtilities extends Mod {

    public static final String PREF_CIRCLE = "carboutil-circle-research";
    static CircleResearchDialog circleDialog;

    public CarboUtilities() {
        Log.info("[CarboUtilities] loaded");
    }

    @Override
    public void init() {
        Log.info("[CarboUtilities] init");
        Events.on(ClientLoadEvent.class, new Cons<ClientLoadEvent>() {
            @Override
            public void get(ClientLoadEvent event) {
                Core.app.post(new Runnable() {
                    @Override
                    public void run() {
                        initSettings();
                        initDialog();
                        wmb.injectPlanetBackground();
                        wmb.injectFlyerUnit();
                    }
                });
            }
        });
    }

    private void initSettings() {
        try {
            Vars.ui.settings.addCategory("CarboUtilities", Icon.settings, new Cons<SettingsTable>() {
                @Override
                public void get(SettingsTable table) {
                    table.checkPref(PREF_CIRCLE, true);
                    table.row();
                    table.add("[lightgray]Круговое древо исследований[]").left().padLeft(12f).row();
                    table.add("[lightgray]Изменение вступает в силу сразу[]").left().padLeft(12f).row();
                }
            });
        } catch (Throwable t) {
            Log.warn("[CarboUtilities] Settings failed: " + t.getMessage());
        }
    }

    private void initDialog() {
        try {
            circleDialog = new CircleResearchDialog();
            Log.info("[CarboUtilities] CircleResearchDialog ready");

            Vars.ui.research.shown(new Runnable() {
                @Override
                public void run() {
                    if (!Core.settings.getBool(PREF_CIRCLE, true)) return;
                    Core.app.post(new Runnable() {
                        @Override
                        public void run() {
                            if (Vars.ui.research.isShown()) {
                                Vars.ui.research.hide();
                            }
                            circleDialog.show();
                        }
                    });
                }
            });

            Log.info("[CarboUtilities] CircleResearchDialog hooked!");
        } catch (Throwable t) {
            Log.err("[CarboUtilities] Dialog init failed", t);
        }
    }
}