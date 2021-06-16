package com.larsluph.timeframecalculator

class ComputeLooper(private val context: ComputeActivity) : Runnable {
    override fun run() {
        while (true) {
            context.updateLoop()
            while (context.isAlreadyLooping) {
                Thread.sleep(15)
            }
            Thread.sleep(10)
        }
    }
}