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

#include <stdint.h>
#include <iostream>

class Shape {

  virtual void dummy() {}

};

class Circle : public Shape {
};

class Rectangle : public Shape {
};

class Window {
};

using namespace std;

int main() {
  // Upcast. Always safe.
  Shape* s = new Circle();
  cout << s << endl;

  // Static downcast. Not really safe.
  Circle* c1 = static_cast<Circle*>(s);
  cout << c1 << endl;

  // Static downcast. Just wrong.
  Rectangle* r1 = static_cast<Rectangle*>(s);
  cout << r1 << endl;

  // Doesn't work, unrelated classes.
  // Window* w = static_cast<Window*>(s);

  // Plain assignment, changes const-ness. Always safe.
  const Circle* c2 = c1;
  cout << c2 << endl;

  // Cast const-ness away again. Dangerous.
  Circle* c3 = const_cast<Circle*>(c2);
  cout << c3 << endl;

  // Treat bits completely differently. Pretty bad.
  intptr_t i = reinterpret_cast<intptr_t>(c3);
  cout << i << endl;

  // Treat bits completely differently. Super bad.
  Window* w = reinterpret_cast<Window*>(s);
  cout << w << endl;

  // Dynamic downcast. Guaranteed to be safe!
  Circle* c4 = dynamic_cast<Circle*>(s);
  cout << c4 << endl;

  // Dynamic downcast. Guaranteed to be safe!
  Rectangle* r2 = dynamic_cast<Rectangle*>(s);
  cout << r2 << endl;

  // Dynamic downcast. Guaranteed to be safe!
  Rectangle& r3 = dynamic_cast<Rectangle&>(*s);
  cout << &r3 << endl;

  return 0;
}
