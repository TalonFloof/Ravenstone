;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;             MONITOR FOR RAVEN-8T             ;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Created by                                   ;
; ▄▄▄▄▄▄ ▄▄▄· ▄▄▌         ▐ ▄ ·▄▄▄      ▐▄• ▄  ;
; ▀•██ ▀▐█ ▀█ ██•   ▄█▀▄ •█▌▐██  · ▄█▀▄  █▌█▌▪ ;
;   ▐█.▪▄█▀▀█ ██ ▪ ▐█▌.▐▌▐█▐▐▌█▀▀▪▐█▌.▐▌ ·██·  ;
;   ▐█▌·▐█▪ ▐▌▐█▌ ▄▐█▌.▐▌██▐█▌██ .▐█▌.▐▌▪▐█·█▌ ;
;   ▀▀▀  ▀  ▀ .▀▀▀  ▀█▄▀▪▀▀ █▪▀▀▀  ▀█▄▀▪•▀▀ ▀▀ ;
;                                              ;
;    Copyright (C) 2022 TalonFox               ;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;Licensed under the Apache License, Version 2.0 (the "License");
;you may not use this file except in compliance with the License.
;You may obtain a copy of the License at
;   http://www.apache.org/licenses/LICENSE-2.0
;Unless required by applicable law or agreed to in writing, software
;distributed under the License is distributed on an "AS IS" BASIS,
;WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;See the License for the specific language governing permissions and
;limitations under the License.

    .org $F000

; These Macros implement opcodes which are exclusive to the Ravenstone
mmu .macro
    .byte $ef
    .byte \0
.endmacro
mas .macro
    .byte $df
.endmacro
; These Macros implement opcodes which are available on the WDC 65C02
wai .macro
    .byte $cb
.endmacro
stp .macro
    .byte $db
.endmacro

cold_boot: ; This function is called when the Raven-8t first starts up
    cld      ; Disable BCD Mode
    lda #$00 ; Clear Registers
    ldx #$00
    ldy #$00
    ; Search for a Teletype that we can use
    lda #$06
    sbc #1
    mmu $
stop:
    jmp stop