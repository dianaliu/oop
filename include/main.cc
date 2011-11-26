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

#include <iostream>
#include <sstream>

#include "java_lang.h"

using namespace java::lang;

int main(void) {
  // Let's get started.
  std::cout << "--------------------------------------------------------------"
            << "----------------"
            << std::endl;

  // Object o = new Object();
  Object o = new __Object();

  std::cout << "o.toString() : "
            << o->__vptr->toString(o) // o.toString()
            << std::endl;

  // Class k = o.getClass();
  __rt::checkNotNull(o);
  Class k = o->__vptr->getClass(o);

  __rt::checkNotNull(k);
  std::cout << "k.getName()  : "
            << k->__vptr->getName(k) // k.getName()
            << std::endl
            << "k.toString() : "
            << k->__vptr->toString(k) // k.toString()
            << std::endl;

  // Class l = k.getClass();
  __rt::checkNotNull(k);
  Class l = k->__vptr->getClass(k);

  __rt::checkNotNull(l);
  std::cout << "l.getName()  : "
            << l->__vptr->getName(l) // l.getName()
            << std::endl
            << "l.toString() : "
            << l->__vptr->toString(l) // l.toString()
            << std::endl;

  // if (k.equals(l)) { ... } else { ... }
  __rt::checkNotNull(k);
  if (k->__vptr->equals(k, l)) {
    std::cout << "k.equals(l)" << std::endl;
  } else {
    std::cout << "! k.equals(l)" << std::endl;
  }

  // if (k.equals(l.getSuperclass())) { ... } else { ... }
  __rt::checkNotNull(k);
  __rt::checkNotNull(l);
  if (k->__vptr->equals(k, l->__vptr->getSuperclass(l))) {
    std::cout << "k.equals(l.getSuperclass())" << std::endl;
  } else {
    std::cout << "! k.equals(l.getSuperclass())" << std::endl;
  }

  // if (k.isInstance(o)) { ... } else { ... }
  __rt::checkNotNull(k);
  if (k->__vptr->isInstance(k, o)) {
    std::cout << "o instanceof k" << std::endl;
  } else {
    std::cout << "! (o instanceof k)" << std::endl;
  }

  // if (l.isInstance(o)) { ... } else { ... }
  __rt::checkNotNull(l);
  if (l->__vptr->isInstance(l, o)) {
    std::cout << "o instanceof l" << std::endl;
  } else {
    std::cout << "! (o instanceof l)" << std::endl;
  }

  // HACK: Calling java.lang.Object.toString on k
  std::cout << o->__vptr->toString(k) << std::endl;

  // An implicit upcast: o = k;
  o = k;

  // An explicit downcast: k = (Class)o;
  k = __rt::java_cast<Class>(o);

  // Having fun with strings.
  std::cout << "--------------------------------------------------------------"
            << "----------------"
            << std::endl;

  // String s1 = "Rose";
  String s1 = __rt::literal("Rose");

  // String s2 = "Robb";
  String s2 = __rt::literal("Robb");

  // s1.equals(o)
  if (s1->__vptr->equals(s1, o)) {
    std::cout << "s1.equals(o)" << std::endl;
  } else {
    std::cout << "! s1.equals(o)" << std::endl;
  }

  // s1.equals(s2)
  if (s1->__vptr->equals(s1, s2)) {
    std::cout << "s1.equals(s2)" << std::endl;
  } else {
    std::cout << "! s1.equals(s2)" << std::endl;
  }

  // s1.length() == s2.length()
  if (s1->__vptr->length(s1) == s2->__vptr->length(s2)) {
    std::cout << "s1.length() == s2.length()" << std::endl;
  } else {
    std::cout << "s1.length() != s2.length()" << std::endl;
  }

  // s1.charAt(1) == s2.charAt(1)
  if (s1->__vptr->charAt(s1, 1) == s2->__vptr->charAt(s2, 1)) {
    std::cout << "s1.charAt(1) == s2.charAt(1)" << std::endl;
  } else {
    std::cout << "s1.charAt(1) != s2.charAt(1)" << std::endl;
  }

  // s1.charAt(2) == s2.charAt(2)
  if (s1->__vptr->charAt(s1, 2) == s2->__vptr->charAt(s2, 2)) {
    std::cout << "s1.charAt(2) == s2.charAt(2)" << std::endl;
  } else {
    std::cout << "s1.charAt(2) != s2.charAt(2)" << std::endl;
  }

  // String s3 = s1 + " and " + s2;
  std::ostringstream temp;
  temp << s1 << " and " << s2;
  String s3 = new java::lang::__String(temp.str());

  // System.out.println(s3);
  std::cout << s3 << std::endl;

  // Having fun with arrays.
  std::cout << "--------------------------------------------------------------"
            << "----------------"
            << std::endl;

  // int[] a = new int[5];
  __rt::Ptr<__rt::Array<int32_t> > a = new __rt::Array<int32_t>(5);

  // a[2]
  // Think of (*a)[2] as a->operator[](2).
  __rt::checkNotNull(a);
  std::cout << "a[2]  : " << (*a)[2] << std::endl;

  // a[2] = 5;
  __rt::checkNotNull(a);
  (*a)[2] = 5;

  // a[2]
  __rt::checkNotNull(a);
  std::cout << "a[2]  : " << (*a)[2] << std::endl;

  // String[] ss = new String[5];
  __rt::Ptr<__rt::Array<String> > ss = new __rt::Array<String>(5);

  // ss[2] = s2;
  __rt::checkNotNull(ss);
  __rt::checkStore(ss, s2);
  (*ss)[2] = s2;

  // System.out.println(ss[2]);
  std::cout << "ss[2] : " << (*ss)[2] << std::endl;

  // Done.
  std::cout << "--------------------------------------------------------------"
            << "----------------"
            << std::endl;
  return 0;
}
