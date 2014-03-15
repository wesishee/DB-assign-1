import java.nio.ByteBuffer;


/*******************************************************************************
 * @file  Conversions.java
 *
 * @author   John Miller
 *
 * @see http://snippets.dzone.com/posts/show/93
 */

/*******************************************************************************
 * This class provides methods for converting Java's primitive data types into
 * byte arrays.
 */
public class Conversions
{
    /***************************************************************************
     * Convert short into a byte array.
     * @param value  the short value to convert
     * @return  a corresponding byte array
     */
    public static byte [] short2ByteArray (short value)
    {
        return new byte [] { (byte) (value >>> 8),
                             (byte) value };
    } // short2ByteArray

    /***************************************************************************
     * Convert int into a byte array.
     * @param value  the int value to convert
     * @return  a corresponding byte array
     */
    public static byte [] int2ByteArray (int value)
    {
        return new byte [] { (byte) (value >>> 24),
                             (byte) (value >>> 16),
                             (byte) (value >>> 8),
                             (byte) value };
    } // int2ByteArray

    /***************************************************************************
     * Convert long into a byte array.
     * @param value  the long value to convert
     * @return  a corresponding byte array
     */
    public static byte [] long2ByteArray (long value)
    {
        return new byte [] { (byte) (value >>> 56),
                             (byte) (value >>> 48),
                             (byte) (value >>> 40),
                             (byte) (value >>> 32),
                             (byte) (value >>> 24),
                             (byte) (value >>> 16),
                             (byte) (value >>> 8),
                             (byte) value };
    } // long2ByteArray

    /***************************************************************************
     * Convert float into a byte array.
     * @param value  the float value to convert
     * @return  a corresponding byte array
     */
    public static byte [] float2ByteArray (float value)
    {
    	return ByteBuffer.allocate(4).putFloat(value).array();
    } // float2ByteArray

    /***************************************************************************
     * Convert double into a byte array.
     * @param value  the double value to convert
     * @return  a corresponding byte array
     */
    public static byte [] double2ByteArray (double value)
    {
    	 return ByteBuffer.allocate(8).putDouble(value).array();
    } // double2ByteArray

} // Conversions

