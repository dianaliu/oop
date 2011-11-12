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

#include "ptr.h"

#include <iostream>

template<typename T>
__rt::Ptr<T> id(__rt::Ptr<T> p) {
  TRACE("id:1:");     return p;
}

int main(void) {
  TRACE("main:1:");   __rt::Ptr<int> p(new int(5));
  TRACE("main:2:");   __rt::Ptr<int> q = id(p);
  TRACE("main:3:");   p = q;
  TRACE("main:4:");   std::cout << *p << std::endl;
  TRACE("main:5:");   delete q;
  TRACE("main:6:");   return 0;
}
