/*
 * Object-Oriented Programming
 * Copyright (C) 2011 Robert Grimm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 * USA.
 */

#pragma once

#include <iostream>

#if 1
#define TRACE(s) std::cout << s << std::endl;
#else
#define TRACE(s)
#endif

namespace __rt {

  template<typename T>
  class Ptr {
    T* addr;

  public:
    Ptr(T* addr) : addr(addr) {
      TRACE("Ptr: constructor");
    }

    Ptr(const Ptr& other) : addr(other.addr) {
      TRACE("Ptr: copy constructor");
    }

    ~Ptr() {
      TRACE("Ptr: destructor");
    }

    Ptr& operator=(const Ptr& right) {
      TRACE("Ptr: assignment operator");
      if (addr != right.addr) {
        addr = right.addr;
      }
      return *this;
    }

    T& operator*() const {
      TRACE("Ptr: dereference");
      return *addr;
    }

    T* operator->() const {
      TRACE("Ptr: arrow");
      return addr;
    }

    operator T*() const {
      TRACE("Ptr: cast operator");
      return addr;
    }
    
  };

}
