package com.fan.logger;


/**
 * 默认格式化日志格式
 *
 * @author fan
 */
public class SimpleLogFormat implements LogFormat {

    private static final char TOP_LEFT_CORNER = '╔';
    private static final char BOTTOM_LEFT_CORNER = '╚';
    private static final char MIDDLE_CORNER = '╟';
    private static final char HORIZONTAL_DOUBLE_LINE = '║';
    private static final String DOUBLE_DIVIDER = "════════════════════════════════════════════";
    private static final String SINGLE_DIVIDER = "────────────────────────────────────────────";
    private static final String TOP_BORDER = TOP_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String BOTTOM_BORDER = BOTTOM_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String MIDDLE_BORDER = MIDDLE_CORNER + SINGLE_DIVIDER + SINGLE_DIVIDER;

    @Override
    public String formatConsole(LogInfo logInfo) {
        StringBuilder sb = new StringBuilder();
        drawTopBorder(sb);

        drawHorizontalDoubleLine(sb);
        sb.append(Utility.dateTimeFormat());
        sb.append("  Thread: ").append(Thread.currentThread().getName()).append("\n");

        drawMiddleBorder(sb);
        appendSplitLines(logInfo.getMessage(), sb);

        drawMiddleBorder(sb);
        appendSplitLines(logInfo.getThrowableInfo(), sb);

        drawBottomBorder(sb);
        return sb.toString();
    }

    private void drawMiddleBorder(StringBuilder sb) {
        sb.append(MIDDLE_BORDER).append("\n");
    }

    private void drawBottomBorder(StringBuilder sb) {
        sb.append(BOTTOM_BORDER).append("\n");
    }

    private void drawHorizontalDoubleLine(StringBuilder sb) {
        sb.append(HORIZONTAL_DOUBLE_LINE).append("  ");
    }

    private void drawTopBorder(StringBuilder sb) {
        sb.append(TOP_BORDER).append("\n");
    }

    private void appendSplitLines(String message, StringBuilder sb) {
        String content = message == null ? "" : message;
        String[] arrays = content.split("\n");
        for (String line : arrays) {
            drawHorizontalDoubleLine(sb);
            sb.append(line).append("\n");
        }
    }

    @Override
    public String formatOutput(LogInfo logInfo) {
        String tag = logInfo.getTag();
        String msg = logInfo.getMessage();
        Level level = logInfo.getLevel();

        StringBuilder sb = new StringBuilder();
        sb.append("<log");
        sb.append(" level=\"");
        sb.append(level);
        sb.append("\" ");
        sb.append("tag=\"");
        sb.append(tag);
        sb.append("\">\n");

        sb.append("<time>");
        sb.append(Utility.dateTimeFormat());
        sb.append("</time>\n");

        sb.append("<msg>");
        sb.append(msg == null ? "" : msg);
        sb.append("</msg>\n");
        sb.append("    <data>\n");
        sb.append(logInfo.getThrowableInfo());
        sb.append("    </data>\n");
        sb.append("</log>\n\n");
        return sb.toString();
    }
}
