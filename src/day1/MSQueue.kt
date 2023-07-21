package day1

import kotlinx.atomicfu.*
import java.util.EmptyStackException

class MSQueue<E> : Queue<E> {
    val head: AtomicRef<Node<E>>
    val tail: AtomicRef<Node<E>>

    init {
        val dummy = Node<E>(null)
        head = atomic(dummy)
        tail = atomic(dummy)
    }

    override fun enqueue(element: E) {
        while(true) {
            val node = Node(element)
            val curTail = tail.value
            if (curTail.next.compareAndSet(null, node)) {
                tail.compareAndSet(curTail, node)
                return
            }
            tail.compareAndSet(curTail, curTail.next.value!!)
        }
    }

    override fun dequeue(): E? {
        while(true) {
            val curHead = head.value
            val curHeadNext = curHead.next.value ?: return null
            if (head.compareAndSet(curHead, curHeadNext)) {
                return curHeadNext.element
            }
        }
    }

    class Node<E>(
        var element: E?
    ) {
        val next = atomic<Node<E>?>(null)
    }
}
