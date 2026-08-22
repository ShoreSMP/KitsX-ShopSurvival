/*
 * This file is part of KitsX
 *
 * KitsX
 * Copyright (c) 2024 XyrisPlugins
 *
 * KitsX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * KitsX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package dev.darkxx.kitsx.hooks.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import dev.darkxx.kitsx.api.events.KitLoadEvent;
import dev.darkxx.kitsx.api.events.KitRoomOpenEvent;
import dev.darkxx.kitsx.api.events.KitSaveEvent;
import dev.darkxx.kitsx.hooks.skript.conditions.CondEqualsKit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Function;

public class SkriptHook {

    @Contract(value = " -> new", pure = true)
    public static @NotNull SkriptHook get() {
        return new SkriptHook();
    }

    public void of() {
        // Kit Load Event
        Skript.registerEvent("Kit Load", SimpleEvent.class, KitLoadEvent.class, "[kit] load");
        registerEventValue(KitLoadEvent.class, Player.class, KitLoadEvent::getPlayer);
        registerEventValue(KitLoadEvent.class, String.class, KitLoadEvent::getKitName);

        // Kit Save Event
        Skript.registerEvent("Kit Save", SimpleEvent.class, KitSaveEvent.class, "[kit] save");
        registerEventValue(KitSaveEvent.class, Player.class, KitSaveEvent::getPlayer);

        // KitRoom Open Event
        Skript.registerEvent("KitRoom Open Event", SimpleEvent.class, KitRoomOpenEvent.class, "[kitroom] open");
        registerEventValue(KitRoomOpenEvent.class, Player.class, KitRoomOpenEvent::getPlayer);

        // Conditions
        Skript.registerCondition(CondEqualsKit.class,
                "[kit] (is|equals|matches) %string%",
                "[kit] (is(n't| not)|is not|doesn't equal|isn't equal to|doesn't match|isn't matching) %string%"
        );
    }

    private static <E extends org.bukkit.event.Event, T> void registerEventValue(
            Class<E> eventClass,
            Class<T> valueClass,
            Function<E, T> getter
    ) {
        try {
            Class<?> converterClass = Class.forName("org.skriptlang.skript.lang.converter.Converter");
            Method modernRegistration = EventValues.class.getMethod(
                    "registerEventValue",
                    Class.class,
                    Class.class,
                    converterClass,
                    int.class
            );
            Object converter = Proxy.newProxyInstance(
                    converterClass.getClassLoader(),
                    new Class<?>[]{converterClass},
                    (proxy, method, args) -> {
                        if (method.getName().equals("convert")) {
                            return getter.apply(eventClass.cast(args[0]));
                        }
                        if (method.getName().equals("toString")) {
                            return "KitsX event value converter";
                        }
                        return null;
                    }
            );
            modernRegistration.invoke(null, eventClass, valueClass, converter, 0);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            EventValues.registerEventValue(eventClass, valueClass, new Getter<T, E>() {
                @Override
                public T get(E event) {
                    return getter.apply(event);
                }
            }, 0);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not register Skript event value", exception);
        }
    }
}
