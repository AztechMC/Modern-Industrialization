/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package aztech.modern_industrialization.thirdparty.fabrictransfer.impl;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.SlottedStorage;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.Storage;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.StorageView;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.base.SingleSlotStorage;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.TransactionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

public class TransferApiImpl {
	public static final Logger LOGGER = LoggerFactory.getLogger("fabric-transfer-api-v1");
	public static final AtomicLong version = new AtomicLong();
	@SuppressWarnings("rawtypes")
	public static final Storage EMPTY_STORAGE = new Storage() {
		@Override
		public boolean supportsInsertion() {
			return false;
		}

		@Override
		public long insert(Object resource, long maxAmount, TransactionContext transaction) {
			return 0;
		}

		@Override
		public boolean supportsExtraction() {
			return false;
		}

		@Override
		public long extract(Object resource, long maxAmount, TransactionContext transaction) {
			return 0;
		}

		@Override
		public Iterator<StorageView> iterator() {
			return Collections.emptyIterator();
		}

		@Override
		public long getVersion() {
			return 0;
		}

		@Override
		public String toString() {
			return "EmptyStorage";
		}
	};

	public static <T> Iterator<T> singletonIterator(T it) {
		return new Iterator<T>() {
			boolean hasNext = true;

			@Override
			public boolean hasNext() {
				return hasNext;
			}

			@Override
			public T next() {
				if (!hasNext) {
					throw new NoSuchElementException();
				}

				hasNext = false;
				return it;
			}
		};
	}

	public static <T> List<SingleSlotStorage<T>> makeListView(SlottedStorage<T> storage) {
		return new AbstractList<>() {
			@Override
			public SingleSlotStorage<T> get(int index) {
				return storage.getSlot(index);
			}

			@Override
			public int size() {
				return storage.getSlotCount();
			}
		};
	}
}
