package nars.bag.impl.experimental.Compressed;

/**
 *
 * * Compression/Decompression
 */
public class Level1 {
    final class HardEnumTypes {
        // term which is just a atomic utf32 string, length is stored as a word
        final static int ATOMICTERM = 0;
    }

    /*
    public Term interpretBytes(byte[] rawAccess, int startIndex) {
        ByteBuffer bb;

        final int type = bb.getChar();

        if( type == HardEnumTypes.ATOMICTERM ) {
            return interpretBytesAtomicTerm(rawAccess, startIndex+1);
        }
    }

    private Term interpretBytesAtomicTerm(byte[] rawAccess, int startIndex) {
        ByteBuffer bb;

        final int length = bb.getShort();

        for( int i = 0; i < length; i++ ) {


            final int signCode = bb.getInt();

            // TODO< add to result >
        }
    }
    */
}
