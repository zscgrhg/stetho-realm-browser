package com.example.tools;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by THINK on 2016/1/9.
 */
public abstract class Convertor<S, T> {

    public static class ToParentConvertor<C extends D, D> extends Convertor<C, D> {
        @Override
        public D convert(C source) throws Exception {
            return source;
        }
    }

    public static class ToStringConvertor<C> extends Convertor<C, String> {
        @Override
        public String convert(C source) throws Exception {
            return source.toString();
        }
    }


    public static class ToDateConvertor<C> extends Convertor<C, Date> {

        @Override
        public Date convert(C source) throws Exception {
            String s = source.toString();
            return stringToDateConvertor.convert(s);
        }
    }

    public static class StringToDateConvertor<C extends CharSequence> extends Convertor<C, Date> {
        public final SimpleDateFormat sdf17 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        public final SimpleDateFormat sdf14 = new SimpleDateFormat("yyyyMMddHHmmss");
        public final SimpleDateFormat sdf8 = new SimpleDateFormat("yyyyMMdd");
        public final SimpleDateFormat sdf6 = new SimpleDateFormat("yyMMdd");
        public final String format = sdf17.format(new Date());

        @Override
        public Date convert(CharSequence source) throws Exception {
            String s = source.toString().replaceAll("\\D+", "");
            Date d;
            switch (s.length()) {
                case 17:
                    d = sdf17.parse(s);
                    break;
                case 14:
                    d = sdf14.parse(s);
                    break;
                case 8:
                    d = sdf8.parse(s);
                    break;
                case 6:
                    d = sdf6.parse(s);
                    break;
                case 4:
                    d = sdf8.parse(format.substring(0, 4) + s);
                    break;
                case 0:
                    throw new RuntimeException(source + " is not a valid Date");
                default:
                    d = new Date(Long.parseLong(s));
            }
            return d;
        }
    }

    public static final ToParentConvertor toParentConvertor = new ToParentConvertor();
    public static final ToStringConvertor toStringConvertor = new ToStringConvertor();

    public static final StringToDateConvertor stringToDateConvertor = new StringToDateConvertor();
    public static final ToDateConvertor TO_DATE_CONVERTOR = new ToDateConvertor();

    public abstract T convert(S source) throws Exception;

    public static <A, B> Convertor<A, B> getConvertor(Class<A> source, Class<B> target) {
        if (target.isAssignableFrom(source)) {
            return toParentConvertor;
        }
        String name = target.getName();
        switch (name) {
            case "java.lang.String":
                return toStringConvertor;
            case "java.util.Date":
                return TO_DATE_CONVERTOR;
            default:
                return null;
        }
    }

}
