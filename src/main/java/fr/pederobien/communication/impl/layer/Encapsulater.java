package fr.pederobien.communication.impl.layer;

import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.ReadableByteWrapper;

import java.util.ArrayList;
import java.util.List;

public class Encapsulater {
	private final byte[] beginWord, endWord;
	private byte[] remaining;

	/**
	 * Creates a layer that encapsulate message to send within two words. When unpacking a raw buffer, the identifier are dummy and
	 * should not be considered.
	 *
	 * @param begin The word to use at the beginning of a request
	 * @param end   The word to use at the end of the request.
	 */
	public Encapsulater(String begin, String end) {
		remaining = new byte[0];
		this.beginWord = begin.getBytes();
		this.endWord = end.getBytes();
	}

	/**
	 * Encapsulater the given bytes array within a begin-word and an end-word.
	 *
	 * @param data The data to encapsulate.
	 * @return A bytes array that contains the begin-word, the data and the end-word.
	 */
	public byte[] pack(byte[] data) {
		ByteWrapper wrapper = ByteWrapper.create();
		wrapper.put(beginWord);
		wrapper.put(data);
		wrapper.put(endWord);
		return wrapper.get();
	}

	/**
	 * Parse the given bytes array in order to extract several requests.
	 *
	 * @param raw The bytes array received from the remote.
	 * @return A list of bytes array. Each bytes array where encapsulated within a begin-word and an end-word.
	 */
	public List<byte[]> unpack(byte[] raw) throws Exception {
		byte[] toParse;

		// Initializing the remaining bytes from the previous state
		if (remaining.length == 0)
			toParse = raw;
		else {
			toParse = new byte[remaining.length + raw.length];
			System.arraycopy(remaining, 0, toParse, 0, remaining.length);
			System.arraycopy(raw, 0, toParse, remaining.length, raw.length);
			remaining = new byte[0];
		}

		ReadableByteWrapper wrapper = ReadableByteWrapper.wrap(toParse);
		List<byte[]> messages = new ArrayList<byte[]>();
		int startIndex = 0;
		int endIndex = 0;
		do {
			// Getting index of BEGIN_WORD
			int start = wrapper.nextIndexOf(beginWord);
			startIndex = start != -1 ? start : startIndex;

			// Getting index of END_WORD
			int end = wrapper.nextIndexOf(endWord);
			endIndex = end != -1 ? end : endIndex;

			// Extracting and registering message
			if ((start != -1) && (end != -1)) {
				// Ignoring BEGIN_WORD
				start += 4;
				messages.add(extract(toParse, start, end - start));
			} else
				break;
		} while (true);

		// Last message not complete
		if (endIndex + endWord.length != toParse.length) {
			int size = toParse.length - startIndex;
			remaining = new byte[size];
			System.arraycopy(toParse, startIndex, remaining, 0, size);
		}

		return messages;
	}

	/**
	 * Extract a bytes array from the given bytes array.
	 *
	 * @param buffer The bytes array from which a bytes array will be extracted.
	 * @param start  The index of the first byte.
	 * @param length The number of bytes to extract from start.
	 * @return A new byte array.
	 */
	public byte[] extract(byte[] buffer, int start, int length) {
		byte[] data = new byte[length];
		System.arraycopy(buffer, start, data, 0, length);
		return data;
	}
}
