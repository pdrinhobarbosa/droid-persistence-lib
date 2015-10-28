package org.dpl;

import org.dpl.R;

import android.net.Uri;

public class TesteProvider extends DplProvider {

	private static final int TESTE_ENTITY_CODE = 0;
	private static final int TESTE_ENTITY_CODE_ID = 1;

	@Override
	public void fillUriMatcher() {
		URI_MATCHER.addURI(getAuthority(getContext()), TesteEntity.class.getSimpleName(), TESTE_ENTITY_CODE);
		URI_MATCHER.addURI(getAuthority(getContext()), TesteEntity.class.getSimpleName() + SEPARATOR_FOR_ID_URI, TESTE_ENTITY_CODE_ID);
	}

	@Override
	public String getTable(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
			case TESTE_ENTITY_CODE:
				return TesteEntity.class.getSimpleName();
			case TESTE_ENTITY_CODE_ID:
				return TesteEntity.class.getSimpleName();
		}

		return null;
	}

	@Override
	public void notifyExtraUris(Uri uri) {}

	@Override
	public Class<?> getRClass() {
		return R.class;
	}

	@Override
	public void deleteRalationships(Uri uri, String selection, String[] selectionArgs) {}

	@Override
	protected void registerContentObeservers() {}
}
