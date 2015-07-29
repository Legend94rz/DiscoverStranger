package org.helloworld.interfaces;

import org.helloworld.tools.ChatEmoji;

/**
 * Created by Administrator on 2015/7/26.
 */
public interface OnCorpusSelectedListener
{
	void onCorpusSelected(ChatEmoji emoji);

	void onCorpusDeleted();
}
