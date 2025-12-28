package ezvcard.io.chain;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import ezvcard.VCard;
import ezvcard.io.ParseWarning;
import ezvcard.io.StreamReader;
import ezvcard.io.scribe.ScribeIndex;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.property.VCardProperty;

/*
 Copyright (c) 2012-2023, Michael Angstadt
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Parent class for all chaining parsers. This class is package-private in order
 * to hide it from the generated Javadocs.
 * @author Michael Angstadt
 * @param <T> the object instance's type (for method chaining)
 */
abstract class ChainingParser<T extends ChainingParser<?>> implements Iterable<VCard> {
	final String string;
	final InputStream in;
	final Reader reader;
	final Path file;

	ScribeIndex index;
	List<List<ParseWarning>> warnings;

	@SuppressWarnings("unchecked")
	final T this_ = (T) this;

	ChainingParser(String string) {
		this(string, null, null, null);
	}

	ChainingParser(InputStream in) {
		this(null, in, null, null);
	}

	ChainingParser(Reader reader) {
		this(null, null, reader, null);
	}

	ChainingParser(Path file) {
		this(null, null, null, file);
	}

	ChainingParser() {
		this(null, null, null, null);
	}

	private ChainingParser(String string, InputStream in, Reader reader, Path file) {
		this.string = string;
		this.in = in;
		this.reader = reader;
		this.file = file;
	}

	/**
	 * Registers a property scribe.
	 * @param scribe the scribe
	 * @return this
	 */
	public T register(VCardPropertyScribe<? extends VCardProperty> scribe) {
		if (index == null) {
			index = new ScribeIndex();
		}
		index.register(scribe);
		return this_;
	}

	/**
	 * Provides a list object that any parser warnings will be put into.
	 * @param warnings the list object that will be populated with the warnings
	 * of each parsed vCard. Each element in the list is a list of warnings for
	 * one parsed vCard. Therefore, the size of this list will be equal to the
	 * number of parsed vCards. If a vCard does not have any warnings, then its
	 * warning list will be empty.
	 * @return this
	 */
	public T warnings(List<List<ParseWarning>> warnings) {
		this.warnings = warnings;
		return this_;
	}

	/**
	 * Reads the first vCard from the stream.
	 * @return the vCard or null if there are no vCards
	 * @throws IOException if there's an I/O problem
	 */
	public VCard first() throws IOException {
		StreamReader reader = newReaderWithIndex();

		try {
            return readNextWithWarnings(reader);
		} finally {
            closeIfNeeded(reader);
        }
	}

	/**
	 * Reads all vCards from the stream.
	 * @return the parsed vCards
	 * @throws IOException if there's an I/O problem
	 */
	public List<VCard> all() throws IOException {
		StreamReader reader = newReaderWithIndex();

		try {
			List<VCard> vcards = new ArrayList<>();
			VCard vcard;
			while ((vcard = readNextWithWarnings(reader)) != null) {
				vcards.add(vcard);
			}
			return vcards;
		} finally {
            closeIfNeeded(reader);
        }
	}

	/**
	 * Lazily reads vCards from the stream.
	 * @return an {@link Iterator} over vCards being parsed
	 */
	@Override
	public Iterator<VCard> iterator() {
		return new ParsingIterator();
    }

	/**
	 * Creates a {@link Stream} of vCards to be lazily read.
	 * @return a {@link Stream} of vCards being parsed
	 */
	public Stream<VCard> stream() {
		ParsingSpliterator spliterator = new ParsingSpliterator();
		return StreamSupport.stream(spliterator, false).onClose(spliterator::close);
	}

	abstract StreamReader constructReader() throws IOException;

	private boolean closeWhenDone() {
		return in == null && reader == null;
	}

	private StreamReader newReaderWithIndex() throws IOException {
		StreamReader reader = constructReader();
		if (index != null) {
			reader.setScribeIndex(index);
		}
		return reader;
	}

	private VCard readNextWithWarnings(StreamReader reader) throws IOException {
		VCard vCard = reader.readNext();
		if (vCard != null && warnings != null) {
			warnings.add(reader.getWarnings());
		}
		return vCard;
	}

	private void closeIfNeeded(StreamReader reader) throws IOException {
		if (closeWhenDone()) {
			reader.close();
		}
	}

	private final class ParsingIterator extends Parserator implements Iterator<VCard> {
		private VCard next;

		@Override
		public boolean hasNext() {
			return !done && (next != null || (next = advance()) != null);
		}

		@Override
		public VCard next() {
			if (done) {
				throw new NoSuchElementException();
			}

			VCard next = this.next;
			if (next != null) {
				this.next = null;
				return next;
			}

			if ((next = advance()) != null) {
				return next;
			}

			throw new NoSuchElementException();
		}
	}

	private final class ParsingSpliterator extends Parserator implements Spliterator<VCard> {
		@Override
		public boolean tryAdvance(Consumer<? super VCard> action) {
			if (done) {
				return false;
			}

			VCard vCard = advance();
			if (vCard != null) {
				action.accept(vCard);
				return true;
			}

			return false;
		}

		@Override
		public Spliterator<VCard> trySplit() {
			return null;
		}

		@Override
		public long estimateSize() {
			return Long.MAX_VALUE;
		}

		@Override
		public int characteristics() {
			return Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE;
		}
	}

	private class Parserator {
		private StreamReader reader;
		boolean done = false;

		VCard advance() {
			StreamReader reader = this.reader;
			if (reader == null) {
				try {
					this.reader = reader = newReaderWithIndex();
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}

			VCard vCard;
			try {
				vCard = readNextWithWarnings(reader);
			} catch (IOException e) {
				throw closeExceptionally(e);
			}

			if (vCard == null) {
				close();
			}
			return vCard;
		}

		void close() {
			StreamReader reader = this.reader;
			this.reader = null;
			done = true;
			try {
				closeIfNeeded(reader);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		private UncheckedIOException closeExceptionally(IOException e) {
			StreamReader reader = this.reader;
			this.reader = null;
			done = true;
			try {
				closeIfNeeded(reader);
			} catch (IOException ex) {
				e.addSuppressed(ex);
			}
			return new UncheckedIOException(e);
		}
	}
}
