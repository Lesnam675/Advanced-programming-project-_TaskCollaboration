package com.aastu.taskmanagersystem.client.ui.styles;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Shared palette and inline styles for JavaFX UI (Stabilized Phase 5).
 */
public final class UiStyles {

    private UiStyles() {}

    public static final String BG_APP =
            "-fx-background-color: linear-gradient(to bottom right, #F4F6F9, #E9ECF5);";

    public static final String FONT_FAMILY =
            "-fx-font-family: 'Segoe UI', 'Inter', 'Helvetica Neue', Arial, sans-serif;";

    public static final String COLOR_TEXT = "#172B4D";
    public static final String COLOR_TEXT_MUTED = "#5E6C84";
    public static final String COLOR_SURFACE = "#FFFFFF";
    public static final String COLOR_BORDER = "#DFE1E6";

    // Modernized Accents & Priorities (Harmonized with Jira/Trello premium feel)
    public static final String ACCENT_TODO = "#0052CC";
    public static final String ACCENT_DOING = "#F2A100";
    public static final String ACCENT_DONE = "#23B27D";

    public static final String PRIORITY_HIGH = "#E05252";
    public static final String PRIORITY_MEDIUM = "#F2A100";
    public static final String PRIORITY_LOW = "#23B27D";

    public static final String BADGE_CREATOR =
            FONT_FAMILY
            + "-fx-background-color: #E3FCEF;"
            + "-fx-text-fill: #006644;"
            + "-fx-font-size: 10px;"
            + "-fx-font-weight: bold;"
            + "-fx-padding: 3 8;"
            + "-fx-background-radius: 12;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,102,68,0.08), 2, 0, 0, 1);";

    public static final String BADGE_ASSIGNEE =
            FONT_FAMILY
            + "-fx-background-color: #E2EEFF;"
            + "-fx-text-fill: #0052CC;"
            + "-fx-font-size: 10px;"
            + "-fx-font-weight: bold;"
            + "-fx-padding: 3 8;"
            + "-fx-background-radius: 12;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,82,204,0.08), 2, 0, 0, 1);";

    public static final String BADGE_OVERDUE =
            FONT_FAMILY
            + "-fx-background-color: #FFEBE6;"
            + "-fx-text-fill: #E05252;"
            + "-fx-font-size: 10px;"
            + "-fx-font-weight: bold;"
            + "-fx-padding: 3 8;"
            + "-fx-background-radius: 12;"
            + "-fx-effect: dropshadow(gaussian, rgba(224,82,82,0.08), 2, 0, 0, 1);";

    public static final String LOGGED_IN_USER =
            FONT_FAMILY
            + "-fx-background-color: #EAE8FF;"
            + "-fx-text-fill: #5243AA;"
            + "-fx-font-weight: bold;"
            + "-fx-font-size: 13px;"
            + "-fx-padding: 6 14;"
            + "-fx-background-radius: 18;"
            + "-fx-border-color: #D3CFFF;"
            + "-fx-border-radius: 18;"
            + "-fx-border-width: 1;"
            + "-fx-effect: dropshadow(gaussian, rgba(82,67,170,0.05), 4, 0, 0, 2);";

    public static String statCard(String accentColor) {
        return FONT_FAMILY
                + "-fx-background-color: rgba(255, 255, 255, 0.95);"
                + "-fx-border-color: " + accentColor + ";"
                + "-fx-border-width: 0 0 0 4;"
                + "-fx-border-radius: 8;"
                + "-fx-background-radius: 8;"
                + "-fx-padding: 12 16;"
                + "-fx-effect: dropshadow(gaussian, rgba(9,30,66,0.08), 8, 0, 0, 2);";
    }

    public static final String ACTIVITY_ENTRY =
            FONT_FAMILY
            + "-fx-background-color: " + COLOR_SURFACE + ";"
            + "-fx-border-color: " + COLOR_BORDER + ";"
            + "-fx-border-radius: 8;"
            + "-fx-background-radius: 8;"
            + "-fx-padding: 8 12;"
            + "-fx-effect: dropshadow(gaussian, rgba(9,30,66,0.03), 4, 0, 0, 1);";

    public static final String NOTIF_ENTRY =
            FONT_FAMILY
            + "-fx-background-color: #FFFDE7;"
            + "-fx-border-color: #F2A100;"
            + "-fx-border-radius: 8;"
            + "-fx-background-radius: 8;"
            + "-fx-padding: 8 12;"
            + "-fx-effect: dropshadow(gaussian, rgba(242,161,0,0.05), 4, 0, 0, 1);";

    public static final String BADGE_PILL =
            FONT_FAMILY
            + "-fx-background-color: #E05252;"
            + "-fx-text-fill: white;"
            + "-fx-font-size: 9px;"
            + "-fx-font-weight: bold;"
            + "-fx-padding: 2 6;"
            + "-fx-background-radius: 10;"
            + "-fx-alignment: center;";

    public static String inputField() {
        return FONT_FAMILY
                + "-fx-background-color: #FAFBFC;"
                + "-fx-border-color: " + COLOR_BORDER + ";"
                + "-fx-border-radius: 8;"
                + "-fx-background-radius: 8;"
                + "-fx-padding: 8 12;"
                + "-fx-font-size: 13px;"
                + "-fx-text-fill: " + COLOR_TEXT + ";";
    }

    public static String primaryButton() {
        return FONT_FAMILY
                + "-fx-background-color: " + ACCENT_TODO + ";"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
                + "-fx-font-size: 13px;"
                + "-fx-background-radius: 8;"
                + "-fx-padding: 8 16;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,82,204,0.1), 4, 0, 0, 2);"
                + "-fx-cursor: hand;";
    }

    public static String secondaryButton() {
        return FONT_FAMILY
                + "-fx-background-color: transparent;"
                + "-fx-text-fill: " + COLOR_TEXT_MUTED + ";"
                + "-fx-border-color: " + COLOR_BORDER + ";"
                + "-fx-border-radius: 8;"
                + "-fx-background-radius: 8;"
                + "-fx-padding: 8 14;"
                + "-fx-cursor: hand;";
    }

    public static String comboBox() {
        return FONT_FAMILY
                + "-fx-background-color: #FAFBFC;"
                + "-fx-border-color: " + COLOR_BORDER + ";"
                + "-fx-border-radius: 8;"
                + "-fx-background-radius: 8;";
    }

    public static void applyButtonEffects(Button button, boolean primary) {
        String baseStyle = primary ? primaryButton() : secondaryButton();
        button.setStyle(baseStyle);

        String hoverBg = primary ? "#0747A6" : "#F4F5F7";
        String hoverText = primary ? "white" : "#172B4D";
        String hoverBorder = primary ? "none" : "#C1C7D0";
        String hoverStyle = baseStyle 
                + "-fx-background-color: " + hoverBg + ";"
                + "-fx-text-fill: " + hoverText + ";"
                + (primary ? "" : "-fx-border-color: " + hoverBorder + ";")
                + "-fx-effect: dropshadow(gaussian, " + (primary ? "rgba(0,82,204,0.2)" : "rgba(9,30,66,0.08)") + ", 6, 0, 0, 3);";

        String pressedBg = primary ? "#003D99" : "#EBECF0";
        String pressedStyle = baseStyle 
                + "-fx-background-color: " + pressedBg + ";"
                + "-fx-text-fill: " + hoverText + ";"
                + "-fx-effect: none;";

        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
        button.setOnMousePressed(e -> button.setStyle(pressedStyle));
        button.setOnMouseReleased(e -> {
            if (button.isHover()) {
                button.setStyle(hoverStyle);
            } else {
                button.setStyle(baseStyle);
            }
        });
    }

    public static void applyFieldEffects(Control field) {
        String baseStyle = field instanceof javafx.scene.control.ComboBox || field instanceof javafx.scene.control.DatePicker ? comboBox() : inputField();
        String hoverBorder = "#A5B2C6";
        String focusBorder = "#0052CC";
        
        String hoverStyle = baseStyle + "-fx-border-color: " + hoverBorder + ";";
        String focusStyle = baseStyle + "-fx-border-color: " + focusBorder + "; -fx-background-color: #FFFFFF; -fx-effect: dropshadow(gaussian, rgba(0,82,204,0.15), 4, 0, 0, 0);";

        field.setStyle(baseStyle);
        field.setOnMouseEntered(e -> {
            if (!field.isFocused()) {
                field.setStyle(hoverStyle);
            }
        });
        field.setOnMouseExited(e -> {
            if (!field.isFocused()) {
                field.setStyle(baseStyle);
            }
        });
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(focusStyle);
            } else {
                field.setStyle(field.isHover() ? hoverStyle : baseStyle);
            }
        });
    }

    public static String getInitials(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        name = name.trim();
        String[] parts = name.split("\\s+");
        if (parts.length >= 2) {
            String first = parts[0].substring(0, 1).toUpperCase();
            String second = parts[1].substring(0, 1).toUpperCase();
            return first + second;
        } else if (name.length() >= 2) {
            return name.substring(0, 2).toUpperCase();
        } else {
            return name.substring(0, 1).toUpperCase();
        }
    }

    public static Label createUserAvatar(String name, String baseColor) {
        String initials = getInitials(name);
        Label avatar = new Label(initials);
        avatar.setStyle(
                FONT_FAMILY
                + "-fx-background-color: " + baseColor + "15;"
                + "-fx-text-fill: " + baseColor + ";"
                + "-fx-font-size: 10px;"
                + "-fx-font-weight: bold;"
                + "-fx-alignment: center;"
                + "-fx-min-width: 24px;"
                + "-fx-min-height: 24px;"
                + "-fx-max-width: 24px;"
                + "-fx-max-height: 24px;"
                + "-fx-background-radius: 12px;"
                + "-fx-border-color: " + baseColor + ";"
                + "-fx-border-radius: 12px;"
                + "-fx-border-width: 1.5px;"
        );
        return avatar;
    }

    public static final String CHAT_BUBBLE =
            FONT_FAMILY
            + "-fx-background-color: #F4F5F7;"
            + "-fx-background-radius: 8;"
            + "-fx-padding: 8 12;";

    public static final String HISTORY_ENTRY =
            FONT_FAMILY
            + "-fx-background-color: transparent;"
            + "-fx-border-color: " + COLOR_BORDER + ";"
            + "-fx-border-width: 0 0 1 0;"
            + "-fx-padding: 8 4;";

    public static final String DOT_ONLINE =
            "-fx-background-color: #36B37E;"
            + "-fx-min-width: 8px;"
            + "-fx-min-height: 8px;"
            + "-fx-max-width: 8px;"
            + "-fx-max-height: 8px;"
            + "-fx-background-radius: 4px;"
            + "-fx-effect: dropshadow(three-pass-box, rgba(54,179,126,0.5), 6, 0.5, 0, 0);";

    public static final String DOT_OFFLINE =
            "-fx-background-color: #7A869A;"
            + "-fx-min-width: 8px;"
            + "-fx-min-height: 8px;"
            + "-fx-max-width: 8px;"
            + "-fx-max-height: 8px;"
            + "-fx-background-radius: 4px;";

    /**
     * Injects highly-polished CSS styles to make scrollbars, dialog panes, and combobox menus visual masterpieces.
     */
    public static void applyGlobalStylesheet(Scene scene) {
        String cssContent = 
                ".scroll-pane {\n" +
                "    -fx-background-color: transparent;\n" +
                "    -fx-background: transparent;\n" +
                "}\n" +
                ".scroll-bar:vertical, .scroll-bar:horizontal {\n" +
                "    -fx-background-color: transparent;\n" +
                "}\n" +
                ".scroll-bar:vertical .thumb, .scroll-bar:horizontal .thumb {\n" +
                "    -fx-background-color: rgba(9, 30, 66, 0.15);\n" +
                "    -fx-background-radius: 6px;\n" +
                "}\n" +
                ".scroll-bar:vertical .thumb:hover, .scroll-bar:horizontal .thumb:hover {\n" +
                "    -fx-background-color: rgba(9, 30, 66, 0.3);\n" +
                "}\n" +
                ".scroll-bar:vertical .increment-button, .scroll-bar:vertical .decrement-button,\n" +
                "sidebar .scroll-bar:horizontal .increment-button, .scroll-bar:horizontal .decrement-button {\n" +
                "    -fx-background-color: transparent;\n" +
                "    -fx-shape: \" \";\n" +
                "}\n" +
                ".combo-box .list-view {\n" +
                "    -fx-background-color: #FFFFFF;\n" +
                "    -fx-background-radius: 8px;\n" +
                "    -fx-border-color: #DFE1E6;\n" +
                "    -fx-border-radius: 8px;\n" +
                "    -fx-effect: dropshadow(gaussian, rgba(9,30,66,0.12), 8, 0, 0, 4);\n" +
                "}\n" +
                ".combo-box .list-cell:hover {\n" +
                "    -fx-background-color: #F4F5F7;\n" +
                "    -fx-text-fill: #172B4D;\n" +
                "}\n" +
                ".tab-pane .tab-header-area {\n" +
                "    -fx-padding: 0 0 4 0;\n" +
                "}\n" +
                ".tab {\n" +
                "    -fx-background-color: transparent;\n" +
                "    -fx-background-radius: 4 4 0 0;\n" +
                "    -fx-padding: 6 12;\n" +
                "}\n" +
                ".tab:selected {\n" +
                "    -fx-background-color: #E2EEFF;\n" +
                "    -fx-border-color: #0052CC;\n" +
                "    -fx-border-width: 0 0 2 0;\n" +
                "}\n" +
                ".dialog-pane {\n" +
                "    -fx-background-color: #FFFFFF;\n" +
                "    -fx-background-radius: 12px;\n" +
                "    -fx-border-radius: 12px;\n" +
                "}\n";

        try {
            String base64Css = URLEncoder.encode(cssContent, StandardCharsets.UTF_8.toString()).replace("+", "%20");
            scene.getStylesheets().add("data:text/css," + base64Css);
        } catch (Exception e) {
            System.err.println("Failed to inject global stylesheet: " + e.getMessage());
        }
    }
}
