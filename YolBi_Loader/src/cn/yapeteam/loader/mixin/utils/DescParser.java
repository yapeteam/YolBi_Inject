package cn.yapeteam.loader.mixin.utils;

import cn.yapeteam.loader.Mapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DescParser {
    static class LMethodDesc {
        String source;
        String[] paramTypes;
        String returnType;

        public LMethodDesc(String content) {
            source = content;
            paramTypes = parseType(cutBetween(content, '(', ')'));
            String[] returnType = parseType(split(content, ")")[1]);
            this.returnType = returnType.length > 0 ? returnType[0] : "";
        }

        public String map() {
            String result = source;
            for (String paramType : paramTypes)
                result = replaceFirst(result, paramType, Mapper.map(null, paramType, null, Mapper.Type.Class));
            result = result.replace(returnType, Mapper.map(null, returnType, null, Mapper.Type.Class));
            return result;
        }
    }

    static class LFieldDesc {
        String source;
        String type;

        public LFieldDesc(String content) {
            source = content;
            String[] values = parseType(content);
            type = values.length > 0 ? values[0] : "";
        }

        public String map() {
            return source.replace(type, Mapper.map(null, type, null, Mapper.Type.Class));
        }
    }

    public static String mapDesc(String content) {
        if (content.contains("("))
            return new LMethodDesc(content).map();
        return new LFieldDesc(content).map();
    }

    public static String replaceFirst(String string, CharSequence target, CharSequence replacement) {
        return Pattern.compile(target.toString(), Pattern.LITERAL).matcher(string).replaceFirst(Matcher.quoteReplacement(replacement.toString()));
    }

    public static String[] split(@NotNull String str, String splitter) {
        if (!str.contains(splitter))
            return new String[]{};
        ArrayList<String> result = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder passed = new StringBuilder();
        for (int i = 0; i < str.length() - (splitter.length() - 1); i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < i + splitter.length(); j++)
                sb.append(str.charAt(j));
            if (sb.toString().equals(splitter)) {
                result.add(stringBuilder.toString());
                passed.append(stringBuilder);
                passed.append(splitter);
                stringBuilder = new StringBuilder();
                i += splitter.length();
            }
            if (i < str.length() - 1)
                stringBuilder.append(str.charAt(i));
        }
        String last = str.replace(passed.toString(), "");
        if (!last.isEmpty())
            result.add(last);
        return result.toArray(new String[0]);
    }

    public static String cutBetween(String content, char start, char end) {
        char[] chars = content.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == start) {
                i++;
                while (chars[i] != end) {
                    builder.append(chars[i]);
                    i++;
                }
                return builder.toString();
            }
        }
        return "";
    }


    public static String[] parseType(String content) {
        char[] chars = content.toCharArray();
        ArrayList<String> types = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == 'L') {
                i++;
                while (chars[i] != ';') {
                    builder.append(chars[i]);
                    i++;
                }
                types.add(builder.toString());
                builder = new StringBuilder();
            }
        }
        return types.toArray(new String[0]);
    }
}
