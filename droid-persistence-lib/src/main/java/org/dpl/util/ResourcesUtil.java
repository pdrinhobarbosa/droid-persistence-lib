package org.dpl.util;

import java.lang.reflect.Field;

import android.content.Context;

public class ResourcesUtil {
	
	/**
	 * Equivalente a {@link #getResourceByName(Class, String, String)}, mas específico para strings.
	 * Retorna a string definida, não seu resourceId.
	 * 
	 * @param context
	 * @param R
	 * 			R.class
	 * @param name
	 * 			O nome do recurso String declarada em <code>R</code>. Por exemplo: <code>R.string.meu_label</code> -> "meu_label"
	 * @return
	 * 			A string definida em <i>strings.xml</i>
	 * @see #getResourceByName(Class, String, String)
	 * @throws Exception
	 * 			Caso o recurso não seja encontrado.
	 */
	public static String getStringResourceByName(Context context, Class<?> R, String name) throws Exception {
		return context.getString(getResourceByName(R, "string", name));
	}
	
	/**
	 * Recupera o ResourceId de um <i>resourceType</i> pelo nome delcarado na classe R.
	 * 
	 * @param R
	 * 			R.class
	 * @param resourceType
	 * 			O tipo de recursos. Por exemplo: <i>"string"</i>, <i>"array"</i>, <i>"id"</i>, <i>"drawable"</i>, etc...
	 * @param name
	 * 			O nome do recurso declarado em <code>R</code>. Por exemplo: <code>R.string.meu_label</code> -> "meu_label"
	 * @return
	 * 			Um inteiro que representa o ResourceId em <code>R</code>
	 * @throws Exception
	 * 			Caso o recurso não seja encontrado.
	 */
	public static int getResourceByName(Class<?> R, String resourceType, String name) throws Exception {
		
		Class<?>[] array = R.getClasses();
		Class<?> resource = null;
		for (Class<?> clazz : array) {
			if (clazz.getSimpleName().equals(resourceType)) {
				resource = clazz;
				break;
			}
		}
		
		Field field = resource.getField(name);
		return field.getInt(name);
	}
}
