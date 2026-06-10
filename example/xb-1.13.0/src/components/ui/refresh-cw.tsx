'use client'

import { motion, useAnimation } from 'motion/react'
import type { HTMLAttributes, MouseEvent } from 'react'
import { forwardRef, useCallback, useImperativeHandle, useRef } from 'react'

import { cn } from '@/lib/utils'

export interface RefreshCWIconHandle {
  startAnimation: () => void
  stopAnimation: () => void
}

interface RefreshCWIconProps extends HTMLAttributes<HTMLDivElement> {
  size?: number
}

const RefreshCWIcon = forwardRef<RefreshCWIconHandle, RefreshCWIconProps>(
  ({ onMouseEnter, onMouseLeave, className, size = 28, ...props }, ref) => {
    const controls = useAnimation()
    const isControlledRef = useRef(false)

    useImperativeHandle(ref, () => {
      isControlledRef.current = true
      return {
        startAnimation: () => controls.start('animate'),
        stopAnimation: () => controls.start('normal'),
      }
    })

    const handleMouseEnter = useCallback(
      (event: MouseEvent<HTMLDivElement>) => {
        onMouseEnter?.(event)

        if (!isControlledRef.current) {
          controls.start('animate')
        }
      },
      [controls, onMouseEnter],
    )

    const handleMouseLeave = useCallback(
      (event: MouseEvent<HTMLDivElement>) => {
        onMouseLeave?.(event)

        if (!isControlledRef.current) {
          controls.start('normal')
        }
      },
      [controls, onMouseLeave],
    )

    return (
      <div onMouseEnter={handleMouseEnter} onMouseLeave={handleMouseLeave} {...props}>
        <motion.svg
          animate={controls}
          className={cn(className)}
          fill="none"
          height={size}
          stroke="currentColor"
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth="2"
          transition={{ type: 'spring', stiffness: 250, damping: 25 }}
          variants={{
            normal: { rotate: '0deg' },
            animate: { rotate: '50deg' },
          }}
          viewBox="0 0 24 24"
          width={size}
          xmlns="http://www.w3.org/2000/svg"
        >
          <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8" />
          <path d="M21 3v5h-5" />
          <path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16" />
          <path d="M8 16H3v5" />
        </motion.svg>
      </div>
    )
  },
)

RefreshCWIcon.displayName = 'RefreshCWIcon'

export { RefreshCWIcon }
