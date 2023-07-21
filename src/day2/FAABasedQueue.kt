package day2

import day1.*
import kotlinx.atomicfu.*

// TODO: Copy the code from `FAABasedQueueSimplified`
// TODO: and implement the infinite array on a linked list
// TODO: of fixed-size segments.
class FAABasedQueue<E> : Queue<E> {
    private val SEGM_SIZE = 2
    class dumby<E>(val arr: AtomicArray<Any?> = atomicArrayOfNulls(2),
                   val id: Int = 0) {
        val next = atomic<dumby<E>?>(null)
    }
    private val enqIdx = atomic(0)
    private val deqIdx = atomic(0)
    private val head: AtomicRef<dumby<E>>
    private val tail: AtomicRef<dumby<E>>
    init {
        val dum = dumby<E>()
        head = atomic(dum)
        tail = atomic(dum)
    }

    private fun getSegm(start: dumby<E>, id: Int): dumby<E> {
        var curStart = start
        while(id > curStart.id) {
            val next = curStart.next.value ?: dumby(id = curStart.id + 1)
            curStart.next.compareAndSet(null, next)
            curStart = curStart.next.value!!
        }
        return curStart
    }

    private fun moveHead() {
        while(true) {
            val curHead = head.value
            val next = curHead.next.value ?: dumby(id=curHead.id + 1)
            if (curHead.next.compareAndSet(null, next)) {
                head.compareAndSet(curHead, next)
                return
            }
            if (head.compareAndSet(curHead, curHead.next.value!!)) return
        }
    }

    private fun moveTail() {
        while (true) {
            val curTail = tail.value
            val next = curTail.next.value ?: dumby(id=curTail.id + 1)
            if (curTail.next.compareAndSet(null, next)) {
                tail.compareAndSet(curTail, next)
                return
            }
            if (tail.compareAndSet(curTail, curTail.next.value!!)) return
        }
    }
    override fun enqueue(element: E) {
        while (true) {
            val curTail = tail.value
            val i = enqIdx.getAndIncrement()
            val s = getSegm(curTail, i / SEGM_SIZE)
            if (i % SEGM_SIZE == SEGM_SIZE - 1) moveTail()
            if (s.arr[i % SEGM_SIZE].compareAndSet(null, element)) {
                return
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun dequeue(): E? {
        while (true) {
            if (deqIdx.value >= enqIdx.value) {
                return null
            }
            val curHead = head.value
            val i = deqIdx.getAndIncrement()
            val s = getSegm(curHead, i / SEGM_SIZE)
            if (i % SEGM_SIZE == SEGM_SIZE - 1) moveHead()
            if (s.arr[i % SEGM_SIZE].compareAndSet(null, POISONED)) {
                continue
            }
            return s.arr[i % SEGM_SIZE].value as E
        }
    }
}

private val POISONED = Any()

