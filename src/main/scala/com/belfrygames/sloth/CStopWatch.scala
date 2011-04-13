package com.belfrygames.sloth

///////////////////////////////////////////////////////////////////////////////
// Simple Stopwatch class. Use this for high resolution timing
// purposes (or, even low resolution timings)
// Pretty self-explanitory....
// Reset(), or GetElapsedSeconds().
class CStopWatch {
  private var m_LastCount = System.nanoTime

  // Resets timer (difference) to zero
  def Reset() = m_LastCount = System.nanoTime

  // Get elapsed time in seconds
  def GetElapsedSeconds() : Float = (System.nanoTime - m_LastCount) / 1000000000.0f
}
