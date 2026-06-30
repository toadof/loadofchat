const ADJECTIVES = [
  'Quiet', 'Blue', 'Swift', 'Happy', 'Cosmic', 'Golden', 'Misty', 'Brave',
  'Calm', 'Neon', 'Wild', 'Silent', 'Lucky', 'Fuzzy', 'Clever', 'Daring',
];

const ANIMALS = [
  'Falcon', 'Otter', 'Panda', 'Fox', 'Wolf', 'Hawk', 'Lynx', 'Bear',
  'Tiger', 'Eagle', 'Dolphin', 'Koala', 'Raven', 'Badger', 'Crane', 'Moose',
];

/**
 * Generate a fun anonymous display name, e.g. "Quiet Falcon".
 */
export function generateNickname() {
  const adj = ADJECTIVES[Math.floor(Math.random() * ADJECTIVES.length)];
  const animal = ANIMALS[Math.floor(Math.random() * ANIMALS.length)];
  return `${adj} ${animal}`;
}

const STORAGE_KEY = 'loadofchat_nickname';

/** Persist nickname per browser tab/session via sessionStorage. */
export function getOrCreateNickname() {
  let name = sessionStorage.getItem(STORAGE_KEY);
  if (!name) {
    name = generateNickname();
    sessionStorage.setItem(STORAGE_KEY, name);
  }
  return name;
}
