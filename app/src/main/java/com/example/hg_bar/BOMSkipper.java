package com.example.hg_bar;

import java.io.IOException;
import java.io.Reader;

/**
 * Created by Looten on 2015-06-26.
 */
public class BOMSkipper
{
    public static void skip(Reader reader) throws IOException
    {
        reader.mark(1);
        char[] possibleBOM = new char[1];
        reader.read(possibleBOM);

        if (possibleBOM[0] != '\ufeff')
        {
            reader.reset();
        }
    }
}
