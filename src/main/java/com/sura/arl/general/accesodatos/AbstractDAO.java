package com.sura.arl.general.accesodatos;

import java.nio.BufferOverflowException;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.sura.arl.reproceso.util.VariablesEntorno;

/**
 * @author agomez
 *
 */
public class AbstractDAO {

    /**
     * The data source.
     */
    private DataSource dataSource;

    /**
     * The jdbc template.
     */
    private JdbcCustomTemplate jdbcTemplate;

    @Autowired
    private VariablesEntorno varEntorno;

    /**
     * Gets the data source.
     *
     * @return the data source
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Sets the data source.
     *
     * @param dataSource the new data source
     */
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcCustomTemplate(dataSource);
    }

    /**
     * Gets the jdbc template.
     *
     * @return the jdbc template
     */
    public JdbcCustomTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    /**
     * Sets the jdbc template.
     *
     * @param jdbcTemplate the new jdbc template
     */
    public void setJdbcTemplate(JdbcCustomTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public VariablesEntorno getVarEntorno() {
        return varEntorno;
    }

    public void setVarEntorno(VariablesEntorno varEntorno) {
        this.varEntorno = varEntorno;
    }

    
    // extendemos NamedParameterJdbcTemplate para agregrar queryForStream
    public class JdbcCustomTemplate extends NamedParameterJdbcTemplate {

        public JdbcCustomTemplate(DataSource dataSource) {
            super(dataSource);
        }

        public <T> Stream<?> queryForStream(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper,
                Object marker) {

            return streamForQuery(100, marker, callback -> {
                // Pasamos un RowCallbackHandler que a su vez pasa MyRows al callback
                getJdbcTemplate().query(sql, paramMap, (rs) -> {
                    callback.accept(rowMapper.mapRow(rs, 0));
                });
                // Pasamos el marker al callback para indicar el fin de stream
                callback.accept(marker);
            });
        }

        private <T> Stream<T> streamForQuery(int bufferSize, T endOfStreamMarker, Consumer<Consumer<T>> query) {
            final LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>(bufferSize);
            // Este es el consumidor que se le pasa al query;
            // recibe cada elemento que el query le pasa y lo pone en la cola.
            Consumer<T> filler = t -> {
                try {
                    // Espera hasta 1 segundo intentando agregar a la cola
                    // Si después de 1 segundo la cola sigue llena, algo grave pasó,
                    // o muy probablemente hubo una operación de corto circuito en el stream.
                    if (!queue.offer(t, 1, TimeUnit.SECONDS)) {
                        // Esta excepción se arroja en el hilo productor para detenerlo.
                        // log.error("Timeout waiting to feed elements to stream");
                        throw new BufferOverflowException();
                    }
                } catch (InterruptedException ex) {
                    System.err.println("Interrupted trying to add item to stream");
                    ex.printStackTrace();
                }
            };
            // Usamos un Spliterator para el stream que devolvemos.
            return StreamSupport
                    .stream(() -> new Spliterators.AbstractSpliterator<T>(Long.MAX_VALUE, Spliterator.ORDERED) {
                        // Esto es para saber si el productor ya arrancó
                        private boolean started = false;
                        // Guardar aquí una excepción si es que ocurre
                        private volatile Throwable boom;

                        /**
                         * Esto se llama una vez, antes de avanzar al primer elemento. Esto arranca el
                         * hilo productor, que corre el query, y le pasamos el filler Definido arriba.
                         */
                        private void startProducer() {
                            // Obtener el hilo consumidor (donde corre esto inicialmente)
                            Thread interruptMe = Thread.currentThread();
                            // Aquí arrancamos el hilo productor
                            new Thread(() -> {
                                try {
                                    // Corre el query con nuestro consumidor especial
                                    query.accept(filler);
                                } catch (BufferOverflowException ignore) {
                                    // El filler arrojó esto, o sea que la cola no está siendo consumida
                                } catch (Throwable thr) {
                                    // Guardar la excepción para que el hilo lector haga algo con ella
                                    boom = thr;
                                    interruptMe.interrupt();
                                }
                            }).start();
                            started = true;
                        }

                        @Override
                        public boolean tryAdvance(Consumer<? super T> action) {
                            if (!started) {
                                startProducer();
                            }
                            try {
                                // Tomar un elemento de la cola y devolverlo, si no es fin de stream
                                // to the action consumer.
                                T t = queue.take();
                                if (t != endOfStreamMarker) {
                                    action.accept(t);
                                    return true;
                                }
                            } catch (InterruptedException ex) {
                                if (boom == null) {
                                    System.err.println("Interrupted reading from stream");
                                    ex.printStackTrace();
                                } else {
                                    // Arrojamos la excepción del productor en el hilo consumidor
                                    throw new RuntimeException(boom);
                                }
                            }
                            return false;
                        }
                    }, Spliterator.IMMUTABLE, false);
        }

    }
}
