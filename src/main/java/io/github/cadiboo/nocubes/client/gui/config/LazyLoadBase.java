package io.github.cadiboo.nocubes.client.gui.config;

import java.util.function.Supplier;

public class LazyLoadBase<T> {

	private Supplier<T> supplier;
	private T value;

	public LazyLoadBase(Supplier<T> supplierIn) {
		this.supplier = supplierIn;
	}

	public T getValue() {
		Supplier<T> supplier = this.supplier;
		if (supplier != null) {
			this.value = supplier.get();
			this.supplier = null;
		}

		return this.value;
	}

}
