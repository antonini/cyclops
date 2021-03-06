package com.aol.cyclops.control;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.data.async.QueueFactories;
import com.aol.cyclops.data.async.Signal;
import com.aol.cyclops.react.threads.SequentialElasticPools;
import com.aol.cyclops.reactor.flux.pushable.MultipleFluxSource;
import com.aol.cyclops.reactor.flux.pushable.PushableFlux;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.util.stream.pushable.MultipleStreamSource;
import com.aol.cyclops.util.stream.pushable.PushableLazyFutureStream;
import com.aol.cyclops.util.stream.pushable.PushableReactiveSeq;
import com.aol.cyclops.util.stream.pushable.PushableStream;


public class FluxSourceTest {
    
   

    @Test
    public void testLazyFutureStream() {
        
        
        
       
        
        PushableLazyFutureStream<Integer> pushable = FluxSource.ofUnbounded()
                                                                 .futureStream(new LazyReact());
        pushable.getInput().add(100);
        pushable.getInput().close();
        assertThat(pushable.getStream().collect(Collectors.toList()),
                hasItem(100));
    }

    @Test
    public void testReactPool() {
        PushableLazyFutureStream<Integer> pushable = FluxSource.ofUnbounded()
                                                                .futureStream(SequentialElasticPools.lazyReact.nextReactor());
        pushable.getInput().add(100);
        pushable.getInput().close();
        assertThat(pushable.getStream().collect(Collectors.toList()),
                hasItem(100));
    }

    @Test
    public void testStreamTuple() {

        Tuple2<Queue<Integer>, Stream<Integer>> pushable = FluxSource.ofUnbounded()
                                                                        .stream();
        pushable.v1.add(10);
        pushable.v1.close();
        assertThat(pushable.v2.collect(Collectors.toList()), hasItem(10));
    }

    @Test
    public void testStream() {
        PushableStream<Integer> pushable = FluxSource.ofUnbounded()
                                                        .stream();
        pushable.getInput().add(10);
        pushable.getInput().close();
        assertThat(pushable.getStream().collect(Collectors.toList()),
                hasItem(10));
    }

    @Test
    public void testStreamBackPressure1() throws InterruptedException {

        PushableStream<Integer> pushable = FluxSource.of(1)
                                                       .stream();
        List events = Collections.synchronizedList(new ArrayList<>());
        new Thread(() -> pushable.getStream().forEach(events::add)).start();
        pushable.getInput().offer(10);
        events.add("here!");
        pushable.getInput().offer(20);
        events.add("there!");
        pushable.getInput().offer(30);
        events.add("there2!");
        pushable.getInput().close();

        System.out.println(events);
        /**
         * non-deterministics assertThat(events.get(0),is("here!"));
         * assertThat(events.get(1),is(10));
         * assertThat(events.get(2),is("there!"));
         * assertThat(events.get(3),is(20));
         * assertThat(events.get(4),is("there2!"));
         * assertThat(events.get(5),is(30));
         **/

    }

    @Test
    public void testStreamBackPressureFlux1() throws InterruptedException {

        PushableFlux<Integer> pushable = FluxSource.of(1)
                                                       .flux();
        List events = Collections.synchronizedList(new ArrayList<>());
        new Thread(() -> pushable.getFlux().subscribe(events::add)).start();
        pushable.getInput().offer(10);
        events.add("here!");
        pushable.getInput().offer(20);
        events.add("there!");
        pushable.getInput().offer(30);
        events.add("there2!");
        pushable.getInput().close();

        System.out.println(events);
        

    }

    @Test
    public void testSeqTuple() {
        Tuple2<Queue<Integer>, ReactiveSeq<Integer>> pushable = FluxSource.ofUnbounded()
                                                                            .reactiveSeq();
        pushable.v1.add(10);
        pushable.v1.close();
        assertThat(pushable.v2.collect(Collectors.toList()), hasItem(10));
    }

    @Test
    public void testSeq() {
        
        PushableReactiveSeq<Integer> pushable = FluxSource.ofUnbounded()
                                                            .reactiveSeq();
        pushable.getInput().add(10);
        pushable.getInput().close();
        
        
        
        assertThat(pushable.getStream().collect(Collectors.toList()),
                hasItem(10));
    }

    @Test
    public void testLazyFutureStreamAdapter() {
        Signal<Integer> signal = Signal.queueBackedSignal();
        LazyFutureStream<Integer> pushable = FluxSource
                                                .futureStream(signal.getDiscrete(),new LazyReact());
        signal.set(100);
        signal.close();
        assertThat(pushable.collect(Collectors.toList()), hasItem(100));
    }

    @Test
    public void testSeqAdapter() {
        Signal<Integer> signal = Signal.queueBackedSignal();
        Seq<Integer> pushable = FluxSource.reactiveSeq(signal
                .getDiscrete());
        signal.set(100);
        signal.close();
        assertThat(pushable.collect(Collectors.toList()), hasItem(100));
    }

    @Test
    public void testStreamAdapter() {
        Signal<Integer> signal = Signal.queueBackedSignal();
        Stream<Integer> pushable = FluxSource
                                        .stream(signal.getDiscrete());
        signal.set(100);
        signal.close();
        assertThat(pushable.collect(Collectors.toList()), hasItem(100));
    }
    
    @Test
    public void testLazyFutureStreamTopic() {
        MultipleFluxSource<Integer> multi = FluxSource
                                                .ofMultiple();
        LazyFutureStream<Integer> pushable = multi
                .futureStream(new LazyReact());
        multi.getInput().offer(100);
        multi.getInput().close();
        assertThat(pushable.collect(Collectors.toList()),
                hasItem(100));
    }
    @Test
    public void testLazyFutureStreamTopicBackPressure() {
        MultipleFluxSource<Integer> multi = FluxSource
                                                .ofMultiple(2);
        LazyFutureStream<Integer> pushable = multi
                .futureStream(new LazyReact());
        multi.getInput().offer(100);
        multi.getInput().close();
        assertThat(pushable.collect(Collectors.toList()),
                hasItem(100));
    }
    @Test
    public void testLazyFutureStreamTopicQueueFactory() {
        MultipleFluxSource<Integer> multi = FluxSource
                                                .ofMultiple(QueueFactories.boundedQueue(100));
        LazyFutureStream<Integer> pushable = multi
                .futureStream(new LazyReact());
        multi.getInput().offer(100);
        multi.getInput().close();
        assertThat(pushable.collect(Collectors.toList()),
                hasItem(100));
    }
    @Test
    public void testReactPoolTopic() {
        MultipleFluxSource<Integer> multi =  FluxSource
                                                    .ofMultiple();
        LazyFutureStream<Integer> pushable = multi
                                        .futureStream(SequentialElasticPools.lazyReact.nextReactor());
        multi.getInput().offer(100);
        multi.getInput().close();
        assertThat(pushable.collect(Collectors.toList()),
                hasItem(100));
    }
    @Test
    public void testStreamTopic() {
        MultipleFluxSource<Integer> multi = FluxSource
                                                        .ofMultiple();
        Stream<Integer> pushable = multi.stream();
        multi.getInput().offer(10);
        multi.getInput().close();
        assertThat(pushable.collect(Collectors.toList()),
                hasItem(10));
    }
    @Test
    public void testSeqTopic() {
        PushableReactiveSeq<Integer> pushable = FluxSource.ofUnbounded()
                                                    .reactiveSeq();
        pushable.getInput().offer(10);
        pushable.getInput().close();
        assertThat(pushable.getStream().collect(Collectors.toList()),
                hasItem(10));
    }
    
    @Test
    public void testMultiple() {
        MultipleFluxSource<Integer> multi = FluxSource
                                                .ofMultiple();
        LazyFutureStream<Integer> pushable = multi
                .futureStream(new LazyReact());
        Seq<Integer> seq = multi.reactiveSeq();
        Stream<Integer> stream = multi.stream();
        multi.getInput().offer(100);
        multi.getInput().close();
        
        Set<Integer> vals = new TreeSet<>();
        pushable.forEach(vals::add);
        seq.forEach(vals::add);
        stream.forEach(vals::add);
        
        assertThat(Sets.newSet(100),is(vals));
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void testWithBackPressureNegativeAfterButOn() {
        PushableLazyFutureStream<Integer> pushable = FluxSource.of(-10)
                                                                  .futureStream(new LazyReact());
        
        pushable.getInput().add(100);
        pushable.getInput().close();
        assertThat(pushable.getStream().collect(Collectors.toList()),
                hasItem(100));
    }
}