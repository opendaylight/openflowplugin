package org.openflow.codec.io;

import java.nio.ByteBuffer;

/**
 * ByteDataBuffer that uses java.nio.ByteBuffer to read/write data.
 *
 * @author AnilGujele
 *
 */
public class ByteDataBuffer implements IDataBuffer {
    private ByteBuffer byteBuffer;

    public ByteDataBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    @Override
    public byte get() {
        return byteBuffer.get();
    }

    @Override
    public IDataBuffer put(byte b) {
        byteBuffer.put(b);
        return this;
    }

    @Override
    public byte get(int index) {

        return byteBuffer.get(index);
    }

    @Override
    public IDataBuffer put(int index, byte b) {
        byteBuffer.put(index, b);
        return this;
    }

    @Override
    public IDataBuffer get(byte[] dst) {
        byteBuffer.get(dst);
        return this;
    }

    @Override
    public IDataBuffer put(byte[] src) {
        byteBuffer.put(src);
        return this;
    }

    @Override
    public char getChar() {
        return byteBuffer.getChar();
    }

    @Override
    public IDataBuffer putChar(char value) {
        byteBuffer.putChar(value);
        return this;

    }

    @Override
    public char getChar(int index) {
        return byteBuffer.getChar(index);
    }

    @Override
    public IDataBuffer putChar(int index, char value) {
        byteBuffer.putChar(index, value);
        return this;
    }

    @Override
    public short getShort() {

        return byteBuffer.getShort();
    }

    @Override
    public IDataBuffer putShort(short value) {
        byteBuffer.putShort(value);
        return this;
    }

    @Override
    public short getShort(int index) {

        return byteBuffer.getShort(index);
    }

    @Override
    public IDataBuffer putShort(int index, short value) {
        byteBuffer.putShort(index, value);
        return this;
    }

    @Override
    public int getInt() {

        return byteBuffer.getInt();
    }

    @Override
    public IDataBuffer putInt(int value) {
        byteBuffer.putInt(value);
        return this;
    }

    @Override
    public int getInt(int index) {

        return byteBuffer.getInt(index);
    }

    @Override
    public IDataBuffer putInt(int index, int value) {
        byteBuffer.putInt(index, value);
        return this;
    }

    @Override
    public long getLong() {

        return byteBuffer.getLong();
    }

    @Override
    public IDataBuffer putLong(long value) {
        byteBuffer.putLong(value);
        return this;
    }

    @Override
    public long getLong(int index) {

        return byteBuffer.getLong(index);
    }

    @Override
    public IDataBuffer putLong(int index, long value) {
        byteBuffer.putLong(index, value);
        return this;
    }

    @Override
    public float getFloat() {

        return byteBuffer.getFloat();
    }

    @Override
    public IDataBuffer putFloat(float value) {
        byteBuffer.putFloat(value);
        return this;
    }

    @Override
    public float getFloat(int index) {

        return byteBuffer.getFloat(index);
    }

    @Override
    public IDataBuffer putFloat(int index, float value) {
        byteBuffer.putFloat(index, value);
        return this;
    }

    @Override
    public double getDouble() {

        return byteBuffer.getDouble();
    }

    @Override
    public IDataBuffer putDouble(double value) {
        byteBuffer.putDouble(value);
        return this;
    }

    @Override
    public double getDouble(int index) {
        return byteBuffer.getDouble(index);
    }

    @Override
    public IDataBuffer putDouble(int index, double value) {
        byteBuffer.putDouble(index, value);
        return this;
    }

    @Override
    public int remaining() {

        return byteBuffer.remaining();
    }

    @Override
    public IDataBuffer mark() {
        byteBuffer.mark();
        return this;
    }

    @Override
    public int position() {

        return byteBuffer.position();
    }

    @Override
    public IDataBuffer position(int newPosition) {
        byteBuffer.position(newPosition);
        return this;
    }

    @Override
    public IDataBuffer reset() {
        byteBuffer.reset();
        return this;
    }

    @Override
    public IDataBuffer clear() {
        byteBuffer.clear();
        return this;
    }

    @Override
    public IDataBuffer flip() {
        byteBuffer.flip();
        return this;
    }

    @Override
    public int limit() {
        // TODO Auto-generated method stub
        return byteBuffer.limit();
    }

    @Override
    public IDataBuffer limit(int newLimit) {
        byteBuffer.limit(newLimit);
        return this;
    }

    @Override
    public IDataBuffer wrap(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        IDataBuffer dataBuffer = new ByteDataBuffer(buffer);
        return dataBuffer;
    }

}
