/*
 * Copyright (c) 2008, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 */

#ifndef CPU_ARM_VM_INTERPRETERRT_ARM_HPP
#define CPU_ARM_VM_INTERPRETERRT_ARM_HPP

#include "memory/allocation.hpp"

// native method calls

class SignatureHandlerGenerator: public NativeSignatureIterator {
 private:
  MacroAssembler* _masm;
  int _abi_offset;
  int  _ireg;

#ifdef __ABI_HARD__
#ifdef AARCH64
  int _freg;
#else
  int _fp_slot; // number of FPR's with arguments loaded
  int _single_fpr_slot;
#endif
#endif

  void move(int from_offset, int to_offset);
  void box(int from_offset, int to_offset);

  void pass_int();
  void pass_long();
  void pass_float();
  void pass_object();
#ifdef __ABI_HARD__
  void pass_double();
#endif
 public:
  // Creation
  SignatureHandlerGenerator(methodHandle method, CodeBuffer* buffer) : NativeSignatureIterator(method) {
    _masm = new MacroAssembler(buffer);
    _abi_offset = 0;
    _ireg = is_static() ? 2 : 1;
#ifdef __ABI_HARD__
#ifdef AARCH64
    _freg = 0;
#else
    _fp_slot = 0;
    _single_fpr_slot = 0;
#endif
#endif
  }

  // Code generation
  void generate(uint64_t fingerprint);

};

#ifndef AARCH64
// ARM provides a normalized fingerprint for native calls (to increase
// sharing). See normalize_fast_native_fingerprint
#define SHARING_FAST_NATIVE_FINGERPRINTS
#endif

#endif // CPU_ARM_VM_INTERPRETERRT_ARM_HPP
