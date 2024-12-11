package jp.co.gxp.javaparser_practice.adaptor.javaparser;

import java.util.function.Supplier;

import com.github.javaparser.utils.Log;

public class LogConfig {

    private LogConfig() {
        // hidden constructor
    }

    /**
     * disable info and trace level logging.
     */
    public static void setErrorOnly() {
        Log.setAdapter(new Log.StandardOutStandardErrorAdapter() {
            @Override
            public void info(Supplier<String> messageSupplier) {
                // stop output
            }

            @Override
            public void trace(Supplier<String> messageSupplier) {
                // stop output
            }
        });
    }
}
