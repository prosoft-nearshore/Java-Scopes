package com.prosoftnearshore.scope;

import org.eclipse.jdt.annotation.Nullable;

public class ChainScope implements Scope {

	public static ChainScope getNew() {
		return new ChainScope();
	}

	/**
	 * Closes this scope, relinquishing the owned resource if any. This method
	 * is invoked automatically on objects managed by the
	 * {@code try-with-resources} statement.
	 *
	 * <p>
	 * If the scope is already closed then invoking this method should have no
	 * effect.
	 *
	 * @throws CloseException
	 *             if the owned resource throws a checked exception. The
	 *             {@code CloseException} would wrap the checked exception
	 *             originally thrown.
	 * 
	 * @throws RuntimeException
	 *             if the owned resource throw an unchecked exception when
	 *             trying to close them.
	 */
	@Override
	public void close() throws RuntimeException {
		final @Nullable AutoCloseable thisHooked = this.hooked;
		if (thisHooked == null) {
			return;
		}

		try {
			thisHooked.close();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new CloseException(e);
		} finally {
			this.hooked = null;
		}

	}

	/**
	 * Hooks a resource into the scope. It is assumed that the resource being
	 * hooked will own any other resource that was previously hooked and so the
	 * scope will only attempt to close the resource that was hooked last.
	 * 
	 * @param resource
	 *            The resource to be hooked in to the scope.
	 * @return The same {@code autoCloseable} passed in.
	 */
	public <T extends AutoCloseable> T hook(T resource) {
		this.hooked = resource;
		return resource;
	}

	public <T extends AutoCloseable> T release(T resource) {
		assert resource == this.hooked : String.format(
				"Attempted to release %s, but what was actually hooked was %s",
				resource, this.hooked);
		
		this.hooked = null;
		return resource;
	}

	/**
	 * Returns a brief description of the object. The exact details of the
	 * representation are unspecified and subject to change.
	 *
	 * @return a brief description of the object.
	 */
	@Override
	public String toString() {
		return String.format("ChainScope{%s}", this.hooked);
	}

	//
	// Private Members
	//
	private @Nullable AutoCloseable hooked = null;

	private ChainScope() {
		// hide constructor from API
	}

}
//
// public <T extends AutoCloseable> T release(T o) {
// assert o == this.hooked : String.format(
// "Attempted to release %s, but what was actually hooked was %s",
// o, ChainScope.this.hooked);
//
// this.hooked = null;
// return o;
// }
//
// public class NotHookedException extends AssertionError {
//
// private NotHookedException(AutoCloseable o) {
// super();
// }
// }
//
// //
// // Private members
// //
// private AutoCloseable hooked = null;
//
// // hide default constructor from clients
// private ChainScope() {
// }
//
// }