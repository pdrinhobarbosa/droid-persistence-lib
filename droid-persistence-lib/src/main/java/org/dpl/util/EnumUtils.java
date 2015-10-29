package org.dpl.util;

import java.util.ArrayList;

import org.dpl.interfaces.EnumInterface;

import android.content.Context;

public class EnumUtils {

    public static ArrayList<Integer> getIds(EnumInterface[] values) {
        ArrayList<Integer> ids = new ArrayList<Integer>();

        for (EnumInterface e : values)
            ids.add(e.getId());

        return ids;
    }

    public static ArrayList<Integer> getKeys(EnumInterface[] values) {
        ArrayList<Integer> keys = new ArrayList<Integer>();

        for (EnumInterface e : values)
            keys.add(e.getI18nKey());

        return keys;
    }

    public static EnumInterface getById(Integer id, EnumInterface[] values) {
        for (EnumInterface e : values)
            if (e.getId() == id)
                return e;

        return null;
    }

    public static ArrayList<EnumInterface> getById(ArrayList<Integer> ids, EnumInterface[] values) {
        ArrayList<EnumInterface> enumList = new ArrayList<EnumInterface>();

        for (Integer id : ids) {
            for (EnumInterface e : values) {
                if (e.getId() == id) {
                    enumList.add(e);
                    break;
                }
            }
        }

        return enumList;
    }

    public static EnumInterface getByOrdinal(Integer ordinal, EnumInterface[] values) {
        return values[ordinal];
    }

    public static ArrayList<EnumInterface> getByOrdinal(ArrayList<Integer> ordinalList, EnumInterface[] values) {
        ArrayList<EnumInterface> grupoSaudeList = new ArrayList<EnumInterface>();

        for (Integer i : ordinalList)
            grupoSaudeList.add(values[i]);

        return grupoSaudeList;
    }

    public static EnumInterface getByKey(Integer i18nKey, EnumInterface[] values) {
        for (EnumInterface e : values)
            if (e.getI18nKey() == i18nKey)
                return e;

        return null;
    }

    public static ArrayList<EnumInterface> getByKey(ArrayList<Integer> i18nKeys, EnumInterface[] values) {
        ArrayList<EnumInterface> enumList = new ArrayList<EnumInterface>();

        for (Integer i18nKey : i18nKeys) {
            for (EnumInterface e : values) {
                if (e.getI18nKey() == i18nKey) {
                    enumList.add(e);
                    break;
                }
            }
        }

        return enumList;
    }

    public static EnumInterface getByString(Context context, String string, EnumInterface[] values) {
        for (EnumInterface enumInterface : values)
            if (context.getString(enumInterface.getI18nKey()).equals(string))
                return enumInterface;

        return null;
    }

    public static ArrayList<EnumInterface> getByString(Context context, ArrayList<String> strings, EnumInterface[] values) {
        ArrayList<EnumInterface> enumList = new ArrayList<EnumInterface>();

        for (String string : strings) {
            for (EnumInterface enumInterface : values) {
                if (context.getString(enumInterface.getI18nKey()).equals(string)) {
                    enumList.add(enumInterface);
                    break;
                }
            }
        }

        return enumList;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static EnumInterface[] getValues(Class clazz, Object... parameters) throws Exception {
        final Class[] parametersType = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++)
            parametersType[i] = parameters[i].getClass();

        return (EnumInterface[]) clazz.getMethod("values", parametersType).invoke(clazz, parameters);
    }

    @SuppressWarnings({"rawtypes"})
    public static int getOrdinal(Enum enumm) throws Exception {
        return enumm.ordinal();
    }
}