/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.johnzon.jsonb;

import org.apache.johnzon.mapper.Mapper;
import org.apache.johnzon.mapper.MapperException;
import org.apache.johnzon.mapper.reflection.JohnzonParameterizedType;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbException;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.CharBuffer;
import java.util.Collection;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

// TODO: Optional handling for lists (and arrays)?
public class JohnsonJsonb implements Jsonb {
    private final Mapper delegate;

    public JohnsonJsonb(final Mapper build) {
        this.delegate = build;
    }

    @Override
    public <T> T fromJson(final String str, final Class<T> type) throws JsonbException {
        try {
            if (isArray(type)) {
                return delegate.readTypedArray(new StringReader(str), type.getComponentType(), type);
            } else if (Collection.class.isAssignableFrom(type)) {
                return (T) delegate.readCollection(new StringReader(str), new JohnzonParameterizedType(type, Object.class));
            }
            final Type mappingType = unwrapPrimitiveOptional(type);
            final Object object = delegate.readObject(str, mappingType);
            if (mappingType != type) {
                return wrapPrimitiveOptional(object, type);
            }
            return (T) object;
        } catch (final MapperException me) {
            throw new JsonbException(me.getMessage(), me);
        }
    }

    private <T> T wrapPrimitiveOptional(final Object object, final Type type) {
        if (OptionalDouble.class == type) {
            if (object == null) {
                return (T) OptionalDouble.empty();
            }
            return (T) OptionalDouble.of(Number.class.cast(object).doubleValue());
        } else if (OptionalInt.class == type) {
            if (object == null) {
                return (T) OptionalInt.empty();
            }
            return (T) OptionalInt.of(Number.class.cast(object).intValue());
        } else if (OptionalLong.class == type) {
            if (object == null) {
                return (T) OptionalLong.empty();
            }
            return (T) OptionalLong.of(Number.class.cast(object).longValue());
        }
        // Optional
        return (T) Optional.ofNullable(object);
    }

    private Type unwrapPrimitiveOptional(final Type type) {
        if (OptionalDouble.class == type) {
            return double.class;
        } else if (OptionalInt.class == type) {
            return int.class;
        } else if (OptionalLong.class == type) {
            return long.class;
        } else if (ParameterizedType.class.isInstance(type)) {
            final ParameterizedType pt = ParameterizedType.class.cast(type);
            if (Optional.class == pt.getRawType()) {
                return pt.getActualTypeArguments()[0];
            }
        }
        return type;
    }

    @Override
    public <T> T fromJson(final String str, final Type runtimeType) throws JsonbException {
        try {
            if (isArray(runtimeType)) {
                final Class cast = Class.class.cast(runtimeType);
                return (T) delegate.readTypedArray(new StringReader(str), cast.getComponentType(), cast);
            } else if (isCollection(runtimeType)) {
                return (T) delegate.readCollection(new StringReader(str), ParameterizedType.class.cast(runtimeType));
            }
            final Type mappingType = unwrapPrimitiveOptional(runtimeType);
            final Object object = delegate.readObject(str, mappingType);
            if (mappingType != runtimeType) {
                return wrapPrimitiveOptional(object, runtimeType);
            }
            return (T) object;
        } catch (final MapperException me) {
            throw new JsonbException(me.getMessage(), me);
        }
    }

    @Override
    public <T> T fromJson(final Readable readable, final Class<T> type) throws JsonbException {
        try {
            if (isArray(type)) {
                return delegate.readTypedArray(toReader(readable), type.getComponentType(), type);
            } else if (Collection.class.isAssignableFrom(type)) {
                return (T) delegate.readCollection(toReader(readable), new JohnzonParameterizedType(type, Object.class));
            }
            final Type mappingType = unwrapPrimitiveOptional(type);
            final Object object = delegate.readObject(toReader(readable), mappingType);
            if (mappingType != type) {
                return wrapPrimitiveOptional(object, type);
            }
            return (T) object;
        } catch (final MapperException me) {
            throw new JsonbException(me.getMessage(), me);
        }
    }

    @Override
    public <T> T fromJson(final Readable readable, final Type runtimeType) throws JsonbException {
        try {
            if (isArray(runtimeType)) {
                final Class<T> type = Class.class.cast(runtimeType);
                return delegate.readTypedArray(toReader(readable), type.getComponentType(), type);
            } else if (isCollection(runtimeType)) {
                return (T) delegate.readCollection(toReader(readable), ParameterizedType.class.cast(runtimeType));
            }
            final Type mappingType = unwrapPrimitiveOptional(runtimeType);
            final Object object = delegate.readObject(toReader(readable), mappingType);
            if (mappingType != runtimeType) {
                return wrapPrimitiveOptional(object, runtimeType);
            }
            return (T) object;
        } catch (final MapperException me) {
            throw new JsonbException(me.getMessage(), me);
        }
    }

    @Override
    public <T> T fromJson(final InputStream stream, final Class<T> type) throws JsonbException {
        try {
            if (isArray(type)) {
                return delegate.readTypedArray(stream, type.getComponentType(), type);
            } else if (Collection.class.isAssignableFrom(type)) {
                return (T) delegate.readCollection(stream, new JohnzonParameterizedType(type, Object.class));
            }
            final Type mappingType = unwrapPrimitiveOptional(type);
            final Object object = delegate.readObject(stream, mappingType);
            if (mappingType != type) {
                return wrapPrimitiveOptional(object, type);
            }
            return (T) object;
        } catch (final MapperException me) {
            throw new JsonbException(me.getMessage(), me);
        }
    }

    @Override
    public <T> T fromJson(final InputStream stream, final Type runtimeType) throws JsonbException {
        try {
            if (isArray(runtimeType)) {
                final Class<T> type = Class.class.cast(runtimeType);
                return delegate.readTypedArray(stream, type.getComponentType(), type);
            } else if (isCollection(runtimeType)) {
                return (T) delegate.readCollection(stream, ParameterizedType.class.cast(runtimeType));
            }

            final Type mappingType = unwrapPrimitiveOptional(runtimeType);
            final Object object = delegate.readObject(stream, mappingType);
            if (mappingType != runtimeType) {
                return wrapPrimitiveOptional(object, runtimeType);
            }
            return (T) object;
        } catch (final MapperException me) {
            throw new JsonbException(me.getMessage(), me);
        }
    }

    @Override
    public String toJson(final Object inObject) throws JsonbException {
        try {
            final Object object = unwrapOptional(inObject);
            if (object == null) {
                return "null";
            }
            if (isArray(object.getClass())) {
                return delegate.writeArrayAsString(toArray(object));
            } else if (Collection.class.isInstance(object)) {
                return delegate.writeArrayAsString(Collection.class.cast(object));
            }
            return delegate.writeObjectAsString(object);

        } catch (final MapperException me) {
            throw new JsonbException(me.getMessage(), me);
        }
    }

    private Object[] toArray(final Object object) {
        final Class<?> componentType = object.getClass().getComponentType();
        Object[] array;
        if (int.class == componentType) {
            final int length = Array.getLength(object);
            array = new Integer[length];
            for (int i = 0; i < length; i++) {
                array[i] = Array.getInt(object, i);
            }
        } else if (double.class == componentType) {
            final int length = Array.getLength(object);
            array = new Integer[length];
            for (int i = 0; i < length; i++) {
                array[i] = Array.getDouble(object, i);
            }
        } else if (byte.class == componentType) {
            final int length = Array.getLength(object);
            array = new Integer[length];
            for (int i = 0; i < length; i++) {
                array[i] = Array.getByte(object, i);
            }
        } else if (char.class == componentType) {
            final int length = Array.getLength(object);
            array = new Integer[length];
            for (int i = 0; i < length; i++) {
                array[i] = Array.getChar(object, i);
            }
        } else if (float.class == componentType) {
            final int length = Array.getLength(object);
            array = new Integer[length];
            for (int i = 0; i < length; i++) {
                array[i] = Array.getFloat(object, i);
            }
        } else if (long.class == componentType) {
            final int length = Array.getLength(object);
            array = new Integer[length];
            for (int i = 0; i < length; i++) {
                array[i] = Array.getLong(object, i);
            }
        } else if (short.class == componentType) {
            final int length = Array.getLength(object);
            array = new Integer[length];
            for (int i = 0; i < length; i++) {
                array[i] = Array.getShort(object, i);
            }
        } else {
            array = (Object[]) object;
        }
        return array;
    }

    @Override
    public String toJson(final Object inObject, final Type runtimeType) throws JsonbException {
        final Object object = unwrapOptional(inObject);
        if (object != null && isArray(runtimeType)) {
            return delegate.writeArrayAsString((Object[]) object);
        } else if (isCollection(runtimeType)) {
            return delegate.writeArrayAsString(Collection.class.cast(object));
        }
        return delegate.writeObjectAsString(object);
    }

    @Override
    public void toJson(final Object inObject, final Appendable appendable) throws JsonbException {
        final Object object = unwrapOptional(inObject);
        if (object != null && isArray(object.getClass())) {
            delegate.writeArray((Object[]) object, toWriter(appendable));
        } else if (Collection.class.isInstance(object)) {
            delegate.writeArray(Collection.class.cast(object), toWriter(appendable));
        } else {
            delegate.writeObject(object, toWriter(appendable));
        }
    }

    @Override
    public void toJson(final Object inObject, final Type runtimeType, final Appendable appendable) throws JsonbException {
        final Object object = unwrapOptional(inObject);
        if (object != null && isArray(runtimeType)) {
            delegate.writeArray((Object[]) object, toWriter(appendable));
        } else if (isCollection(runtimeType)) {
            delegate.writeArray(Collection.class.cast(object), toWriter(appendable));
        } else {
            delegate.writeObject(object, toWriter(appendable));
        }
    }

    @Override
    public void toJson(final Object inObject, final OutputStream stream) throws JsonbException {
        final Object object = unwrapOptional(inObject);
        if (object != null && isArray(object.getClass())) {
            delegate.writeArray((Object[]) object, stream);
        } else if (Collection.class.isInstance(object)) {
            delegate.writeArray(Collection.class.cast(object), stream);
        } else {
            delegate.writeObject(object, stream);
        }
    }

    @Override
    public void toJson(final Object inObject, final Type runtimeType, final OutputStream stream) throws JsonbException {
        final Object object = unwrapOptional(inObject);
        if (object != null && isArray(runtimeType)) {
            delegate.writeArray((Object[]) object, stream);
        } else if (isCollection(runtimeType)) {
            delegate.writeArray(Collection.class.cast(object), stream);
        } else {
            delegate.writeObject(object, stream);
        }
    }

    private Object unwrapOptional(final Object inObject) {
        if (Optional.class.isInstance(inObject)) {
            return Optional.class.cast(inObject).orElse(null);
        }
        if (OptionalInt.class.isInstance(inObject)) {
            final OptionalInt optionalInt = OptionalInt.class.cast(inObject);
            return optionalInt.isPresent() ? optionalInt.getAsInt() : null;
        }
        if (OptionalLong.class.isInstance(inObject)) {
            final OptionalLong optionalLong = OptionalLong.class.cast(inObject);
            return optionalLong.isPresent() ? optionalLong.getAsLong() : null;
        }
        if (OptionalDouble.class.isInstance(inObject)) {
            final OptionalDouble optionalDouble = OptionalDouble.class.cast(inObject);
            return optionalDouble.isPresent() ? optionalDouble.getAsDouble() : null;
        }
        return inObject;
    }

    private boolean isArray(final Type runtimeType) {
        return Class.class.isInstance(runtimeType) && Class.class.cast(runtimeType).isArray();
    }

    private boolean isCollection(final Type runtimeType) {
        if (!ParameterizedType.class.isInstance(runtimeType)) {
            return false;
        }
        final Type rawType = ParameterizedType.class.cast(runtimeType).getRawType();
        return Class.class.isInstance(rawType) && Collection.class.isAssignableFrom(Class.class.cast(rawType));
    }

    private Writer toWriter(final Appendable appendable) {
        return Writer.class.isInstance(appendable) ? Writer.class.cast(appendable) :
            new Writer() {
                @Override
                public void write(final char[] cbuf, final int off, final int len) throws IOException {
                    appendable.append(new String(cbuf, off, len));
                }

                @Override
                public void flush() throws IOException {
                    if (Flushable.class.isInstance(appendable)) {
                        Flushable.class.cast(appendable);
                    }
                }

                @Override
                public void close() throws IOException {
                    if (Closeable.class.isInstance(appendable)) {
                        Closeable.class.cast(appendable);
                    }
                }
            };
    }

    private Reader toReader(final Readable readable) {
        return Reader.class.isInstance(readable) ? Reader.class.cast(readable) :
            new Reader() {
                @Override
                public int read(final char[] cbuf, final int off, final int len) throws IOException {
                    int r;
                    final CharBuffer cb = CharBuffer.allocate(len);
                    while ((r = readable.read(cb)) >= 0) {
                        System.arraycopy(cb.array(), 0, cbuf, off, r);
                    }
                    return readable.read(CharBuffer.allocate(len));
                }

                @Override
                public void close() throws IOException {
                    if (Closeable.class.isInstance(readable)) {
                        Closeable.class.cast(readable);
                    }
                }
            };
    }
}
