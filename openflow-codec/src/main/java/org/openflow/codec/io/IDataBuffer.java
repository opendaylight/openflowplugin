package org.openflow.codec.io;

/**
 * Adaptor interface that declares all the methods that need to be implemented
 * by IO library used by openflow protocol plugin.
 *
 * @see {org.openflow.codec.io.ByteDataBuffer}
 *
 * @author AnilGujele
 *
 */
public interface IDataBuffer {
    /**
     * Relative get method. Reads the byte at this buffer's current position,
     * and then increments the position.
     *
     * @return The byte at the buffer's current position
     *
     * @throws IndexOutOfBoundsException
     *             If the buffer's current position is not smaller than its
     *             limit
     */
    public byte get();

    /**
     * Writes the given byte into this buffer at the current position, and then
     * increments the position.
     *
     * @param b
     *            The byte to be written
     *
     * @return This buffer
     *
     * @throws IndexOutOfBoundsException
     *             If this buffer's current position is not smaller than its
     *             limit
     *
     */
    public IDataBuffer put(byte b);

    /**
     * Absolute get method. Reads the byte at the given index.
     *
     * @param index
     *            The index from which the byte will be read
     *
     * @return The byte at the given index
     *
     * @throws IndexOutOfBoundsException
     *             If index is negative or not smaller than the buffer's limit
     */
    public byte get(int index);

    /**
     * Writes the given byte into this buffer at the given index.
     *
     * @param index
     *            The index at which the byte will be written
     *
     * @param b
     *            The byte value to be written
     *
     * @return This buffer
     *
     * @throws IndexOutOfBoundsException
     *             If index is negative or not smaller than the buffer's limit
     *
     */
    public IDataBuffer put(int index, byte b);

    /**
     * This method transfers bytes from this buffer into the given destination
     * array.
     *
     * @return This buffer
     *
     * @throws IndexOutOfBoundsException
     *             If there are fewer than length bytes remaining in this buffer
     */
    public IDataBuffer get(byte[] dst);

    /**
     * This method transfers the entire content of the given source byte array
     * into this buffer.
     *
     * @return This buffer
     *
     * @throws IndexOutOfBoundsException
     *             If there is insufficient space in this buffer
     *
     */
    public IDataBuffer put(byte[] src);

    /**
     * Reads the next two bytes at this buffer's current position, composing
     * them into a char value according to the current byte order, and then
     * increments the position by two.
     *
     *
     * @return The char value at the buffer's current position
     *
     * @throws IndexOutOfBoundsException
     *             If there are fewer than two bytes remaining in this buffer
     */
    public char getChar();

    /**
     * Writes two bytes containing the given char value, in the current byte
     * order, into this buffer at the current position, and then increments the
     * position by two.
     *
     * @param value
     *            The char value to be written
     *
     * @return This buffer
     *
     * @throws IndexOutOfBoundsException
     *             If there are fewer than two bytes remaining in this buffer
     *
     */
    public IDataBuffer putChar(char value);

    /**
     * Reads two bytes at the given index, composing them into a char value
     * according to the current byte order.
     *
     * @param index
     *            The index from which the bytes will be read
     *
     * @return The char value at the given index
     *
     * @throws IndexOutOfBoundsException
     *             If index is negative or not smaller than the buffer's limit,
     *             minus one
     */
    public char getChar(int index);

    /**
     * Writes two bytes containing the given char value, in the current byte
     * order, into this buffer at the given index.
     *
     * @param index
     *            The index at which the bytes will be written
     *
     * @param value
     *            The char value to be written
     *
     * @return This buffer
     *
     * @throws IndexOutOfBoundsException
     *             If index is negative or not smaller than the buffer's limit,
     *             minus one
     *
     */
    public IDataBuffer putChar(int index, char value);

    /**
     * Reads the next two bytes at this buffer's current position, composing
     * them into a short value according to the current byte order, and then
     * increments the position by two.
     *
     * @return The short value at the buffer's current position
     *
     * @throws IndexOutOfBoundsException
     *             If there are fewer than two bytes remaining in this buffer
     */
    public short getShort();

    /**
     * Writes two bytes containing the given short value, in the current byte
     * order, into this buffer at the current position, and then increments the
     * position by two.
     *
     * @param value
     *            The short value to be written
     *
     * @return This buffer
     *
     * @throws IndexOutOfBoundsException
     *             If there are fewer than two bytes remaining in this buffer
     *
     */
    public IDataBuffer putShort(short value);

    /**
     * Reads two bytes at the given index, composing them into a short value
     * according to the current byte order.
     *
     * @param index
     *            The index from which the bytes will be read
     *
     * @return The short value at the given index
     *
     * @throws IndexOutOfBoundsException
     *             If index is negative or not smaller than the buffer's limit,
     *             minus one
     */
    public short getShort(int index);

    /**
     * Writes two bytes containing the given short value, in the current byte
     * order, into this buffer at the given index.
     *
     * @param index
     *            The index at which the bytes will be written
     *
     * @param value
     *            The short value to be written
     *
     * @return This buffer
     *
     * @throws IndexOutOfBoundsException
     *             If index is negative or not smaller than the buffer's limit,
     *             minus one
     */
    public IDataBuffer putShort(int index, short value);

    /**
     * Reads the next four bytes at this buffer's current position, composing
     * them into an int value according to the current byte order, and then
     * increments the position by four.
     *
     * @return The int value at the buffer's current position
     *
     * @throws IndexOutOfBoundsException
     *             If there are fewer than four bytes remaining in this buffer
     */
    public int getInt();

    /**
     * Writes four bytes containing the given int value, in the current byte
     * order, into this buffer at the current position, and then increments the
     * position by four.
     *
     * @param value
     *            The int value to be written
     *
     * @return This buffer
     *
     * @throws IndexOutOfBoundsException
     *             If there are fewer than four bytes remaining in this buffer
     *
     */
    public IDataBuffer putInt(int value);

    /**
     * Reads four bytes at the given index, composing them into a int value
     * according to the current byte order.
     *
     * @param index
     *            The index from which the bytes will be read
     *
     * @return The int value at the given index
     *
     * @throws IndexOutOfBoundsException
     *             If index is negative or not smaller than the buffer's limit,
     *             minus three
     */
    public int getInt(int index);

    /**
     * Writes four bytes containing the given int value, in the current byte
     * order, into this buffer at the given index.
     *
     * @param index
     *            The index at which the bytes will be written
     *
     * @param value
     *            The int value to be written
     *
     * @return This buffer
     *
     * @throws IndexOutOfBoundsException
     *             If index is negative or not smaller than the buffer's limit,
     *             minus three
     */
    public IDataBuffer putInt(int index, int value);

    /**
     * Reads the next eight bytes at this buffer's current position, composing
     * them into a long value according to the current byte order, and then
     * increments the position by eight.
     *
     * @return The long value at the buffer's current position
     *
     * @throws IndexOutOfBoundsException
     *             If there are fewer than eight bytes remaining in this buffer
     */
    public long getLong();

    /**
     * Writes eight bytes containing the given long value, in the current byte
     * order, into this buffer at the current position, and then increments the
     * position by eight.
     *
     * @param value
     *            The long value to be written
     *
     * @return This buffer
     *
     * @throws IndexOutOfBoundsException
     *             If there are fewer than eight bytes remaining in this buffer
     *
     */
    public IDataBuffer putLong(long value);

    /**
     * Reads eight bytes at the given index, composing them into a long value
     * according to the current byte order.
     *
     * @param index
     *            The index from which the bytes will be read
     *
     * @return The long value at the given index
     *
     * @throws IndexOutOfBoundsException
     *             If index is negative or not smaller than the buffer's limit,
     *             minus seven
     */
    public long getLong(int index);

    /**
     * Writes eight bytes containing the given long value, in the current byte
     * order, into this buffer at the given index.
     *
     * @param index
     *            The index at which the bytes will be written
     *
     * @param value
     *            The long value to be written
     *
     * @return This buffer
     *
     * @throws IndexOutOfBoundsException
     *             If index is negative or not smaller than the buffer's limit,
     *             minus seven
     *
     */
    public IDataBuffer putLong(int index, long value);

    /**
     * Reads the next four bytes at this buffer's current position, composing
     * them into a float value according to the current byte order, and then
     * increments the position by four.
     *
     * @return The float value at the buffer's current position
     *
     * @throws IndexOutOfBoundsException
     *             If there are fewer than four bytes remaining in this buffer
     */
    public float getFloat();

    /**
     * Writes four bytes containing the given float value, in the current byte
     * order, into this buffer at the current position, and then increments the
     * position by four.
     *
     * @param value
     *            The float value to be written
     *
     * @return This buffer
     *
     * @throws IndexOutOfBoundsException
     *             If there are fewer than four bytes remaining in this buffer
     */
    public IDataBuffer putFloat(float value);

    /**
     * Reads four bytes at the given index, composing them into a float value
     * according to the current byte order.
     *
     * @param index
     *            The index from which the bytes will be read
     *
     * @return The float value at the given index
     *
     * @throws IndexOutOfBoundsException
     *             If index is negative or not smaller than the buffer's limit,
     *             minus three
     */
    public float getFloat(int index);

    /**
     * Writes four bytes containing the given float value, in the current byte
     * order, into this buffer at the given index.
     *
     * @param index
     *            The index at which the bytes will be written
     *
     * @param value
     *            The float value to be written
     *
     * @return This buffer
     *
     * @throws IndexOutOfBoundsException
     *             If index is negative or not smaller than the buffer's limit,
     *             minus three
     *
     */
    public IDataBuffer putFloat(int index, float value);

    /**
     * Reads the next eight bytes at this buffer's current position, composing
     * them into a double value according to the current byte order, and then
     * increments the position by eight.
     *
     * @return The double value at the buffer's current position
     *
     * @throws IndexOutOfBoundsException
     *             If there are fewer than eight bytes remaining in this buffer
     */
    public double getDouble();

    /**
     * Writes eight bytes containing the given double value, in the current byte
     * order, into this buffer at the current position, and then increments the
     * position by eight.
     *
     * @param value
     *            The double value to be written
     *
     * @return This buffer
     *
     * @throws IndexOutOfBoundsException
     *             If there are fewer than eight bytes remaining in this buffer
     *
     */
    public IDataBuffer putDouble(double value);

    /**
     * Reads eight bytes at the given index, composing them into a double value
     * according to the current byte order.
     *
     * @param index
     *            The index from which the bytes will be read
     *
     * @return The double value at the given index
     *
     * @throws IndexOutOfBoundsException
     *             If index is negative or not smaller than the buffer's limit,
     *             minus seven
     */
    public double getDouble(int index);

    /**
     * Writes eight bytes containing the given double value, in the current byte
     * order, into this buffer at the given index.
     *
     * @param index
     *            The index at which the bytes will be written
     *
     * @param value
     *            The double value to be written
     *
     * @return This buffer
     *
     * @throws IndexOutOfBoundsException
     *             If index is negative or not smaller than the buffer's limit,
     *             minus seven
     *
     */
    public IDataBuffer putDouble(int index, double value);

    /**
     * Returns the number of elements between the current position and the
     * limit.
     *
     * @return The number of elements remaining in this buffer
     */
    public int remaining();

    /**
     * Sets this buffer's mark at its position.
     *
     * @return This buffer
     */
    public IDataBuffer mark();

    /**
     * Returns this buffer's position.
     *
     * @return The position of this buffer
     */
    public int position();

    /**
     * Sets this buffer's position. If the mark is defined and larger than the
     * new position then it is discarded.
     *
     * @param newPosition
     *            The new position value; must be non-negative and no larger
     *            than the current limit
     *
     * @return This buffer
     *
     * @throws IllegalArgumentException
     *             If the preconditions on newPosition do not hold
     */
    public IDataBuffer position(int newPosition);

    /**
     * Resets this buffer's position to the previously-marked position.
     *
     * Invoking this method neither changes nor discards the mark's value.
     *
     * @return This buffer
     *
     */
    public IDataBuffer reset();

    /**
     * Clears this buffer. The position is set to zero, the limit is set to the
     * capacity, and the mark is discarded.
     *
     * Invoke this method before using a sequence of channel-read or put
     * operations to fill this buffer. For example:
     *
     * buf.clear(); // Prepare buffer for reading in.read(buf); // Read data
     *
     * This method does not actually erase the data in the buffer, but it is
     * named as if it did because it will most often be used in situations in
     * which that might as well be the case.
     *
     * @return This buffer
     */
    public IDataBuffer clear();

    /**
     * Flips this buffer. The limit is set to the current position and then the
     * position is set to zero. If the mark is defined then it is discarded.
     *
     * After a sequence of channel-read or put operations, invoke this method to
     * prepare for a sequence of channel-write or relative get operations. For
     * example:
     *
     * buf.put(magic); // Prepend header in.read(buf); // Read data into rest of
     * buffer buf.flip(); // Flip buffer out.write(buf); // Write header + data
     * to channel
     *
     * @return This buffer
     */
    public IDataBuffer flip();

    /**
     * Returns this buffer's limit.
     *
     * @return The limit of this buffer
     */
    public int limit();

    /**
     * Sets this buffer's limit. If the position is larger than the new limit
     * then it is set to the new limit. If the mark is defined and larger than
     * the new limit then it is discarded.
     *
     * @param newLimit
     *            The new limit value; must be non-negative and no larger than
     *            this buffer's capacity
     *
     * @return This buffer
     *
     * @throws IllegalArgumentException
     *             If the preconditions on newLimit do not hold
     */
    public IDataBuffer limit(int newLimit);

    /**
     * Wraps a data byte array into a buffer.
     *
     * The new buffer will be backed by the given byte array; that is,
     * modifications to the buffer will cause the array to be modified and vice
     * versa. The new buffer's capacity and limit will be array.length, its
     * position will be zero, and its mark will be undefined. Its backing array
     * will be the given array, and its array offset will be zero.
     *
     * @param array
     *            The array that will back this buffer
     *
     * @return The new data buffer
     */
    public IDataBuffer wrap(byte[] array);

}
