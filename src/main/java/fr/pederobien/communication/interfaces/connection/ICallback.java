package fr.pederobien.communication.interfaces.connection;

public interface ICallback {

    /**
     * @return The maximum time, in ms, to wait for remote response.
     */
    int timeout();

    /**
     * Execute the underlying callback with the given arguments.
     */
    void apply(CallbackArgs args);

    record CallbackArgs(int identifier, byte[] response, boolean isTimeout, boolean isConnectionLost) {
        /**
         * Creates a callback argument class to execute a callback depending upon a response has been received from the remote.
         *
         * @param identifier       The identifier of the response.
         * @param response         The byte array received from the remote.
         * @param isTimeout        True if the remote did not answer in time, false
         *                         otherwise.
         * @param isConnectionLost True if the connection with the remote has been lost,
         *                         false otherwise.
         */
        public CallbackArgs {
        }

        /**
         * @return The identifier of the response.
         */
        @Override
        public int identifier() {
            return identifier;
        }

        /**
         * @return The bytes array received from the remote, or null if a timeout
         * occurred.
         */
        @Override
        public byte[] response() {
            return response;
        }

        /**
         * @return true if timeout happened before reception of the response. In this
         * case Response is null.
         */
        @Override
        public boolean isTimeout() {
            return isTimeout;
        }

        /**
         * @return True if the connection with the remote has been lost, false
         * otherwise.
         */
        @Override
        public boolean isConnectionLost() {
            return isConnectionLost;
        }
    }
}
