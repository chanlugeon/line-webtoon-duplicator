package io.github.chanlugeon.lwduplicator.cui;

import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;

public abstract class Console {

    public static void print(Object o) {
        LocalTime time = LocalTime.now();
        String min = Integer.toString(time.getMinute());
        String sec = Integer.toString(time.getSecond());
        String str = time.getHour() + ":" + (min.length() > 1 ? min : "0" + min) + ":" +
                (sec.length() > 1 ? sec : "0" + sec);

        System.out.print(str + " " + o);
    }

    public static void println(Object o) {
        print(o + "\n");
    }

    public static class Box {
        private static final char BRICK_X = '-';
        private static final char BRICK_Y = '|';
        private static final char BRICK_CORNER = '+';
        private static final char WHITESPACE = ' ';

        private List<Line> lines = new LinkedList<>();

        private int width = 50;

        public Box() {}

        public Box(int width) {
            this.width = width;
        }

        public Box line(String content, Alignment align) {
            String[] arr = content.split("\n");
            if (arr.length > 1) {
                for(String a : arr) line(a, align); // Support auto newline.
                return this;
            }

            int len = content.length();
            if (len > width - 4) {
                for (int i = 0; i < len; i += width - 4) {
                    if (i >= width - 4) {
                        lines.add(new Line(content.substring(i), align));
                        return this;
                    }

                    lines.add(new Line(content.substring(i, i + width - 4), align));
                }
            }

            this.lines.add(new Line(content, align));
            return this;
        }

        public Box newline() {
            return line("", Alignment.LEFT);
        }

        public void print() {
            StringBuilder wallX = new StringBuilder(width);
            wallX.append(BRICK_CORNER);
            for (int i = 2; i < width; i++) wallX.append(BRICK_X);
            wallX.append(BRICK_CORNER);
            System.out.println(wallX);

            for (Line line : lines) {
                String c = line.content;
                StringBuilder builder = new StringBuilder(width);
                builder.append(BRICK_Y).append(WHITESPACE);
                int w = width - 4 - c.length();
                int left = (int) Math.floor(w / 2);

                StringBuilder side = new StringBuilder(left);
                for (int i = 0; i < left; i++) side.append(WHITESPACE);

                switch (line.alignment) {
                case LEFT:
                    builder.append(c).append(side)
                    .append(w % 2 > 0 ? side.append(WHITESPACE) : side);
                    break;

                case CENTER:
                    builder.append(side).append(c)
                    .append(w % 2 > 0 ? side.append(WHITESPACE) : side);
                    break;

                case RIGHT:
                    builder.append(side).append(w % 2 > 0 ? side.append(WHITESPACE) : side)
                    .append(c);
                }

                builder.append(WHITESPACE).append(BRICK_Y);
                System.out.println(builder);
            }

            System.out.println(wallX.append('\n'));
        }

        private static class Line {
            public final String content;
            public final Alignment alignment;

            public Line(String content, Alignment align) {
                this.content = content;
                alignment = align;
            }
        }

        public enum Alignment {
            LEFT,
            CENTER,
            RIGHT;
        }
    }
}
